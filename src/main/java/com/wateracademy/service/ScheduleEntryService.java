package com.wateracademy.service;

import com.wateracademy.dto.mapper.ScheduleEntryMapper;
import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.response.ScheduleEntryResponse;
import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.exception.InvalidStatusTransitionException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.ScheduleEntryRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ScheduleEntryService {

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
    public ScheduleEntryResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public ScheduleEntryResponse create(Long workspaceId, ScheduleEntryRequest request) {
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

        return mapper.toResponse(repository.save(entity));
    }

    public ScheduleEntryResponse update(Long id, ScheduleEntryRequest request) {
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
}
