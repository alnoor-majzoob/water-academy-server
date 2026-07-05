package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.TaskStatus;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    Long workspaceId,
    TaskStatus status,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    String log,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
