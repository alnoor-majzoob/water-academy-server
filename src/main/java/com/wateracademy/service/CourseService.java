package com.wateracademy.service;

import com.wateracademy.dto.mapper.CourseMapper;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.response.CourseFilterOptionsResponse;
import com.wateracademy.dto.response.CourseResponse;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.entity.Course;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CourseRepository;
import com.wateracademy.util.PaginationUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "externalId", "name", "specialization", "durationDays", "expectedTrainees",
            "city", "beneficiary", "priority", "type", "earliestStart", "latestEnd", "createdAt", "updatedAt");

    private final CourseRepository repository;
    private final CourseMapper mapper;
    private final WorkspaceService workspaceService;

    public CourseService(CourseRepository repository, CourseMapper mapper,
                         WorkspaceService workspaceService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                              List<String> sort, String search, CourseType type,
                                                              String priority, String city, String specialization) {
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("name").ascending());
        var spec = (org.springframework.data.jpa.domain.Specification<Course>) (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("workspace").get("id"), workspaceId));
            var like = PaginationUtils.like(search);
            if (like != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("externalId")), like),
                        cb.like(cb.lower(root.get("beneficiary")), like)));
            }
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (blankToNull(priority) != null) predicates.add(cb.equal(root.get("priority"), priority));
            if (blankToNull(city) != null) predicates.add(cb.equal(root.get("city"), city));
            if (blankToNull(specialization) != null) predicates.add(cb.equal(root.get("specialization"), specialization));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CourseFilterOptionsResponse filterOptions(Long workspaceId) {
        return new CourseFilterOptionsResponse(
                repository.findDistinctCities(workspaceId),
                List.of(CourseType.values()),
                repository.findDistinctPriorities(workspaceId),
                repository.findDistinctSpecializations(workspaceId));
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public CourseResponse create(Long workspaceId, CourseRequest request) {
        validateCrossField(request);
        var workspace = workspaceService.findEntity(workspaceId);
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public CourseResponse update(Long id, CourseRequest request) {
        validateCrossField(request);
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    private void validateCrossField(CourseRequest request) {
        if (request.earliestStart() != null && request.latestEnd() != null
                && request.earliestStart().isAfter(request.latestEnd())) {
            throw new IllegalArgumentException("earliestStart must be before latestEnd");
        }
        if (request.fixedDate() != null && request.earliestStart() != null
                && request.fixedDate().isBefore(request.earliestStart())) {
            throw new IllegalArgumentException("fixedDate must not be before earliestStart");
        }
        if (request.fixedDate() != null && request.latestEnd() != null
                && request.fixedDate().isAfter(request.latestEnd())) {
            throw new IllegalArgumentException("fixedDate must not be after latestEnd");
        }
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Course findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
