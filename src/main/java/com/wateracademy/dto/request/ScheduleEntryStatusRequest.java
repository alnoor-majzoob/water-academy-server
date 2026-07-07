package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.ScheduleStatus;
import jakarta.validation.constraints.NotNull;

public record ScheduleEntryStatusRequest(
    @NotNull ScheduleStatus status
) {}