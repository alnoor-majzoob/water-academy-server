package com.wateracademy.service;

import com.wateracademy.dto.mapper.CalendarDayMapper;
import com.wateracademy.dto.request.CalendarDayRequest;
import com.wateracademy.dto.response.CalendarDayResponse;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.entity.CalendarDay;
import com.wateracademy.exception.DuplicateResourceException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CalendarDayRepository;
import com.wateracademy.util.PaginationUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CalendarDayService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "date", "isWorkDay", "isHoliday", "createdAt");

    private static final Logger log = LoggerFactory.getLogger(CalendarDayService.class);

    private final CalendarDayRepository repository;
    private final CalendarDayMapper mapper;
    private final WorkspaceService workspaceService;

    public CalendarDayService(CalendarDayRepository repository, CalendarDayMapper mapper,
                              WorkspaceService workspaceService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
    }

    @Transactional(readOnly = true)
    public List<CalendarDayResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CalendarDayResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                                   List<String> sort, LocalDate from, LocalDate to,
                                                                   Boolean isWorkDay, Boolean isHoliday) {
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("date").ascending());
        var spec = (org.springframework.data.jpa.domain.Specification<CalendarDay>) (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("workspace").get("id"), workspaceId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            if (isWorkDay != null) predicates.add(cb.equal(root.get("isWorkDay"), isWorkDay));
            if (isHoliday != null) predicates.add(cb.equal(root.get("isHoliday"), isHoliday));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CalendarDayResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public CalendarDayResponse create(Long workspaceId, CalendarDayRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        if (repository.findByWorkspaceIdAndDate(workspaceId, request.date()).isPresent()) {
            throw new DuplicateResourceException(
                    "Calendar day already exists for date " + request.date());
        }
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public List<CalendarDayResponse> bulkCreate(Long workspaceId, List<CalendarDayRequest> requests) {
        log.info("Bulk creating calendar days: workspaceId={}, count={}", workspaceId, requests.size());
        var workspace = workspaceService.findEntity(workspaceId);
        var entities = requests.stream()
                .map(req -> {
                    var entity = mapper.toEntity(req);
                    entity.setWorkspace(workspace);
                    return entity;
                })
                .toList();
        return repository.saveAll(entities).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public CalendarDayResponse update(Long id, CalendarDayRequest request) {
        var entity = findEntity(id);
        entity.setDate(request.date());
        entity.setIsWorkDay(request.isWorkDay() != null ? request.isWorkDay() : entity.getIsWorkDay());
        entity.setIsHoliday(request.isHoliday() != null ? request.isHoliday() : entity.getIsHoliday());
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    CalendarDay findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarDay", id));
    }
}
