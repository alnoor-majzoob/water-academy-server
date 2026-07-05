package com.wateracademy.service;

import com.wateracademy.dto.mapper.WorkspaceMapper;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.dto.request.WorkspaceStatusRequest;
import com.wateracademy.dto.response.WorkspaceResponse;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.WorkspaceStatus;
import com.wateracademy.exception.InvalidStatusTransitionException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.WorkspaceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkspaceService {

    private final WorkspaceRepository repository;
    private final WorkspaceMapper mapper;

    public WorkspaceService(WorkspaceRepository repository, WorkspaceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public WorkspaceResponse create(WorkspaceRequest request) {
        var entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    public WorkspaceResponse update(Long id, WorkspaceRequest request) {
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public WorkspaceResponse updateStatus(Long id, WorkspaceStatusRequest request) {
        var entity = findEntity(id);
        var newStatus = request.status();
        validateStatusTransition(entity.getStatus(), newStatus);
        entity.setStatus(newStatus);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Workspace findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace", id));
    }

    private void validateStatusTransition(WorkspaceStatus current, WorkspaceStatus next) {
        if (current == WorkspaceStatus.DISABLED) {
            throw new InvalidStatusTransitionException("Cannot transition from DISABLED");
        }
        if (current == WorkspaceStatus.OPTIMIZED && next != WorkspaceStatus.DISABLED) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from OPTIMIZED to " + next);
        }
        if (current == next) {
            return;
        }
        if (current == WorkspaceStatus.IMPORTED && next != WorkspaceStatus.OPTIMIZED
                && next != WorkspaceStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot transition from IMPORTED to " + next);
        }
    }
}
