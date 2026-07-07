package com.wateracademy.service;

import com.wateracademy.dto.mapper.CourseMapper;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.response.CourseResponse;
import com.wateracademy.entity.Course;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CourseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseService {

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
}
