package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.WorkspaceStatus;
import java.time.Instant;

public record WorkspaceResponse(
    Long id,
    String name,
    String description,
    Integer year,
    WorkspaceStatus status,
    String color,
    Instant createdAt,
    Instant updatedAt
) {}