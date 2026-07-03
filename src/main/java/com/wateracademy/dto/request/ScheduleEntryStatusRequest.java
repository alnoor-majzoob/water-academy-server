package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.ScheduleStatus;

public record ScheduleEntryStatusRequest(
    ScheduleStatus status
) {}