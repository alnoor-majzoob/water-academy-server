package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.TaskStatus;
import java.time.Instant;

public record TaskResponse(
    Long id,
    Long workspaceId,
    String mode,
    TaskStatus status,
    Instant startedAt,
    Instant completedAt,
    String log,
    Instant createdAt,
    Instant updatedAt
) {}