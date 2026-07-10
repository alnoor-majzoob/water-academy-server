package com.wateracademy.service;

import com.wateracademy.dto.mapper.ScheduleEntryMapper;
import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.ScheduleEntryFilterOptionsResponse;
import com.wateracademy.dto.response.ScheduleEntryResponse;
import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.exception.InvalidStatusTransitionException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.ScheduleEntryRepository;
import com.wateracademy.util.PaginationUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleEntryService {

    private static final Logger log = LoggerFactory.getLogger(ScheduleEntryService.class);
    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "startDate", "endDate", "status", "createdAt", "updatedAt");

    private final ScheduleEntryRepository repository;
    private final ScheduleEntryMapper mapper;
    private final WorkspaceService workspaceService;
    private final CourseService courseService;
    private final TrainerService trainerService;
    private final VenueService venueService;

    public ScheduleEntryService(ScheduleEntryRepository repository,
                                ScheduleEntryMapper mapper,
                                WorkspaceService workspaceService,
                                CourseService courseService,
                                TrainerService trainerService,
                                VenueService venueService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
        this.courseService = courseService;
        this.trainerService = trainerService;
        this.venueService = venueService;
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleEntryResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                                     List<String> sort, ScheduleStatus status, String city,
                                                                     String month, LocalDate from, LocalDate to,
                                                                     Long trainerId, Long venueId, Long courseId,
                                                                     Boolean hasConflict) {
        var range = monthRange(month, from, to);
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("startDate").ascending());
        return PageResponse.from(repository.searchByWorkspaceId(
                workspaceId, status, blankToNull(city), range.from(), range.to(), trainerId, venueId, courseId,
                hasConflict, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ScheduleEntryFilterOptionsResponse filterOptions(Long workspaceId) {
        var months = repository.findByWorkspaceId(workspaceId).stream()
                .map(ScheduleEntry::getStartDate)
                .map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .distinct()
                .sorted()
                .toList();
        return new ScheduleEntryFilterOptionsResponse(
                repository.findDistinctCities(workspaceId),
                List.of(ScheduleStatus.values()),
                months,
                List.of(Boolean.TRUE, Boolean.FALSE));
    }

    @Transactional(readOnly = true)
    public ScheduleEntryResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public ScheduleEntryResponse create(Long workspaceId, ScheduleEntryRequest request) {
        validateCrossField(request);
        var workspace = workspaceService.findEntity(workspaceId);
        var course = courseService.findEntity(request.courseId());
        var trainer = trainerService.findEntity(request.trainerId());

        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        entity.setCourse(course);
        entity.setTrainer(trainer);
        entity.setStatus(ScheduleStatus.SCHEDULED);

        if (request.venueId() != null) {
            var venue = venueService.findEntity(request.venueId());
            entity.setVenue(venue);
        }

        detectAndSetConflicts(entity);

        if (entity.getConflictNotes() != null) {
            log.warn("Schedule conflict detected: workspaceId={}, courseId={}, trainerId={}, venueId={}, notes={}",
                    workspaceId, entity.getCourse().getId(), entity.getTrainer().getId(),
                    entity.getVenue() != null ? entity.getVenue().getId() : null,
                    entity.getConflictNotes());
        }

        return mapper.toResponse(repository.save(entity));
    }

    public ScheduleEntryResponse update(Long id, ScheduleEntryRequest request) {
        validateCrossField(request);
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);

        if (request.venueId() != null) {
            var venue = venueService.findEntity(request.venueId());
            entity.setVenue(venue);
        } else {
            entity.setVenue(null);
        }

        detectAndSetConflicts(entity);
        return mapper.toResponse(repository.save(entity));
    }

    public ScheduleEntryResponse updateStatus(Long id, ScheduleStatus newStatus) {
        var entity = findEntity(id);
        validateStatusTransition(entity.getStatus(), newStatus);
        log.info("Schedule entry status change: id={}, {} -> {}", id, entity.getStatus(), newStatus);
        entity.setStatus(newStatus);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> findVenueConflicts(Long workspaceId, Long venueId,
                                                           LocalDate startDate, LocalDate endDate) {
        return repository.findVenueConflicts(workspaceId, venueId, startDate, endDate)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ScheduleEntryResponse> findTrainerConflicts(Long workspaceId, Long trainerId,
                                                             LocalDate startDate, LocalDate endDate) {
        return repository.findTrainerConflicts(workspaceId, trainerId, startDate, endDate)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    private void validateCrossField(ScheduleEntryRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
    }

    ScheduleEntry findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduleEntry", id));
    }

    private void detectAndSetConflicts(ScheduleEntry entity) {
        var workspaceId = entity.getWorkspace().getId();
        var notes = new StringBuilder();

        if (entity.getVenue() != null) {
            var venueConflicts = repository.findVenueConflicts(
                    workspaceId, entity.getVenue().getId(),
                    entity.getStartDate(), entity.getEndDate());
            for (var conflict : venueConflicts) {
                if (!conflict.getId().equals(entity.getId())) {
                    notes.append("Venue conflict with ")
                            .append(conflict.getCourse().getName())
                            .append(" (").append(conflict.getStartDate())
                            .append(" - ").append(conflict.getEndDate()).append("). ");
                }
            }
        }

        var trainerConflicts = repository.findTrainerConflicts(
                workspaceId, entity.getTrainer().getId(),
                entity.getStartDate(), entity.getEndDate());
        for (var conflict : trainerConflicts) {
            if (!conflict.getId().equals(entity.getId())) {
                notes.append("Trainer conflict with ")
                        .append(conflict.getCourse().getName())
                        .append(" (").append(conflict.getStartDate())
                        .append(" - ").append(conflict.getEndDate()).append("). ");
            }
        }

        entity.setConflictNotes(notes.isEmpty() ? null : notes.toString().trim());
    }

    private void validateStatusTransition(ScheduleStatus current, ScheduleStatus next) {
        if (current == ScheduleStatus.COMPLETED) {
            throw new InvalidStatusTransitionException("Cannot change status of a completed entry");
        }
        if (current == ScheduleStatus.CONFIRMED && next == ScheduleStatus.SCHEDULED) {
            return;
        }
        if (current == ScheduleStatus.CONFIRMED && next != ScheduleStatus.COMPLETED) {
            throw new InvalidStatusTransitionException(
                    "Can only transition CONFIRMED to COMPLETED");
        }
    }

    private DateRange monthRange(String month, LocalDate from, LocalDate to) {
        if (month == null || month.isBlank()) {
            return new DateRange(from, to);
        }
        var start = LocalDate.parse(month.trim() + "-01");
        return new DateRange(start, start.withDayOfMonth(start.lengthOfMonth()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
