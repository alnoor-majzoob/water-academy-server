package com.wateracademy.service;

import com.wateracademy.dto.mapper.VenueMapper;
import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.response.VenueResponse;
import com.wateracademy.entity.Venue;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.VenueRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VenueService {

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
    public List<VenueResponse> findAllByWorkspaceId(UUID workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VenueResponse findById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    public VenueResponse create(UUID workspaceId, VenueRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public VenueResponse update(UUID id, VenueRequest request) {
        var entity = findEntity(id);
        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(UUID id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Venue findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", id));
    }
}
