package com.wateracademy.dto.mapper;

import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.dto.response.WorkspaceResponse;
import com.wateracademy.entity.Workspace;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    public WorkspaceResponse toResponse(Workspace entity) {
        return new WorkspaceResponse(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getYear(),
            entity.getStatus(),
            entity.getColor(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public Workspace toEntity(WorkspaceRequest request) {
        var workspace = new Workspace();
        workspace.setName(request.name());
        workspace.setDescription(request.description());
        workspace.setYear(request.year());
        workspace.setColor(request.color());
        return workspace;
    }

    public void updateEntity(Workspace entity, WorkspaceRequest request) {
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setYear(request.year());
        entity.setColor(request.color());
    }
}
