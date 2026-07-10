package com.wateracademy.service;

import com.wateracademy.dto.mapper.TrainerMapper;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.TrainerFilterOptionsResponse;
import com.wateracademy.dto.response.TrainerResponse;
import com.wateracademy.entity.Trainer;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.TrainerRepository;
import com.wateracademy.util.PaginationUtils;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrainerService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "externalId", "name", "city", "trainerType", "maxDaysPerMonth",
            "maxConsecutiveDays", "costPerDay", "createdAt", "updatedAt");

    private final TrainerRepository repository;
    private final TrainerMapper mapper;
    private final WorkspaceService workspaceService;

    public TrainerService(TrainerRepository repository, TrainerMapper mapper,
                          WorkspaceService workspaceService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
    }

    @Transactional(readOnly = true)
    public List<TrainerResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<TrainerResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                               List<String> sort, String search, String city,
                                                               String trainerType, String specialty) {
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("name").ascending());
        var spec = (org.springframework.data.jpa.domain.Specification<Trainer>) (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("workspace").get("id"), workspaceId));
            var like = PaginationUtils.like(search);
            if (like != null) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("externalId")), like),
                        cb.like(cb.lower(root.get("specialties")), like)));
            }
            if (blankToNull(city) != null) predicates.add(cb.equal(root.get("city"), city));
            if (blankToNull(trainerType) != null) predicates.add(cb.equal(root.get("trainerType"), trainerType));
            var specialtyLike = PaginationUtils.like(specialty);
            if (specialtyLike != null) predicates.add(cb.like(cb.lower(root.get("specialties")), specialtyLike));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return PageResponse.from(repository.findAll(spec, pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public TrainerFilterOptionsResponse filterOptions(Long workspaceId) {
        var specialties = repository.findByWorkspaceId(workspaceId).stream()
                .flatMap(trainer -> Arrays.stream((trainer.getSpecialties() == null ? "" : trainer.getSpecialties()).split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .toList();
        return new TrainerFilterOptionsResponse(
                repository.findDistinctCities(workspaceId),
                repository.findDistinctTrainerTypes(workspaceId),
                specialties);
    }

    @Transactional(readOnly = true)
    public TrainerResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public TrainerResponse create(Long workspaceId, TrainerRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public TrainerResponse update(Long id, TrainerRequest request) {
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Trainer findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", id));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
