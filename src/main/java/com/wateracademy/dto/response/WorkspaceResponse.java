package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.WorkspaceStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record WorkspaceResponse(
    UUID id,
    String name,
    String description,
    Integer year,
    WorkspaceStatus status,
    String color,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
