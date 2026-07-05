package com.wateracademy.dto.response;

import java.time.LocalDateTime;

public record CourseAssignmentResponse(
    Long id,
    Long workspaceId,
    Long trainerId,
    Long courseId,
    LocalDateTime createdAt
) {}
