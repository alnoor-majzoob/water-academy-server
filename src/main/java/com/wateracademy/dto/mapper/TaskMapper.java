package com.wateracademy.dto.mapper;

import com.wateracademy.dto.response.TaskResponse;
import com.wateracademy.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task entity) {
        return new TaskResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getMode(),
            entity.getStatus(),
            entity.getStartedAt(),
            entity.getCompletedAt(),
            entity.getLog(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
