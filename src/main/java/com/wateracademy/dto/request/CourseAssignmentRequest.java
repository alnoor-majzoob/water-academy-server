package com.wateracademy.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CourseAssignmentRequest(
    @NotNull UUID trainerId,
    @NotNull UUID courseId
) {}
