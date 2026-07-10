package com.wateracademy.service;

import com.wateracademy.dto.mapper.VenueMapper;
import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.VenueFilterOptionsResponse;
import com.wateracademy.dto.response.VenueResponse;
import com.wateracademy.entity.Venue;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.VenueRepository;
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
public class VenueService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "externalId", "name", "city", "capacity", "type", "availableFrom",
            "availableTo", "createdAt", "updatedAt");

    private final VenueRepository repository;
    private final VenueMapper mapper;
    private final WorkspaceService workspaceService;

    public VenueService(VenueRepository repository, VenueMapper mapper,
                        WorkspaceService workspaceService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
    }

    @Transactional(readOnly = true)
    public List<VenueResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<VenueResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                             List<String> sort, String search, String city,
                                                             CourseType type, Integer minCapacity,
                                                             Integer maxCapacity) {
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("name").ascending());
        var spec = (org.springframework.data.jpa.domain.Specification<Venue>) (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("workspace").get("id"), workspaceId));
            var like = PaginationUtils.like(search);
            if (like != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("externalId")), like),
                        cb.like(cb.lower(root.get("equipmentNotes")), like)));
            }
            if (blankToNull(city) != null) predicates.add(cb.equal(root.get("city"), city));
            if (type != null) predicates.add(cb.equal(root.get("type"), type));
            if (minCapacity != null) predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
            if (maxCapacity != null) predicates.add(cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public VenueFilterOptionsResponse filterOptions(Long workspaceId) {
        return new VenueFilterOptionsResponse(repository.findDistinctCities(workspaceId), List.of(CourseType.values()));
    }

    @Transactional(readOnly = true)
    public VenueResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public VenueResponse create(Long workspaceId, VenueRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public VenueResponse update(Long id, VenueRequest request) {
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Venue findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
