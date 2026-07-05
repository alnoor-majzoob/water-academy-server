package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.WorkspaceStatus;
import java.time.LocalDateTime;

public record WorkspaceResponse(
    Long id,
    String name,
    String description,
    Integer year,
    WorkspaceStatus status,
    String color,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
