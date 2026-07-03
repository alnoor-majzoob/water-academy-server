package com.wateracademy.service;

import com.wateracademy.dto.mapper.CourseMapper;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.response.CourseResponse;
import com.wateracademy.entity.Course;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CourseRepository;
import java.util.List;
import java.util.UUID;
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
    public List<CourseResponse> findAllByWorkspaceId(UUID workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    public CourseResponse create(UUID workspaceId, CourseRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public CourseResponse update(UUID id, CourseRequest request) {
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(UUID id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Course findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }
}
