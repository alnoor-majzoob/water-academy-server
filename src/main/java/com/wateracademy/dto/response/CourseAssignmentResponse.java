package com.wateracademy.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CourseAssignmentResponse(
    UUID id,
    UUID workspaceId,
    UUID trainerId,
    UUID courseId,
    LocalDateTime createdAt
) {}
