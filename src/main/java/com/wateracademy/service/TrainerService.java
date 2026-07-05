package com.wateracademy.service;

import com.wateracademy.dto.mapper.TrainerMapper;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.response.TrainerResponse;
import com.wateracademy.entity.Trainer;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.TrainerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrainerService {

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
}
