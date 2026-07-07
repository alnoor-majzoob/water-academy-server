package com.wateracademy.dto.response;

import java.time.Instant;

public record CourseAssignmentResponse(
    Long id,
    Long workspaceId,
    Long trainerId,
    Long courseId,
    Instant createdAt
) {}