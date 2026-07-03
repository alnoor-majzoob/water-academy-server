package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID workspaceId,
    TaskStatus status,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    String log,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
