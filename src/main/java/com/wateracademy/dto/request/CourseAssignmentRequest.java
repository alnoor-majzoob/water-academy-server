package com.wateracademy.dto.request;

import jakarta.validation.constraints.NotNull;

public record CourseAssignmentRequest(
    @NotNull Long trainerId,
    @NotNull Long courseId
) {}
