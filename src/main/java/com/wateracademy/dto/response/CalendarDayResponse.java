package com.wateracademy.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record CalendarDayResponse(
    Long id,
    Long workspaceId,
    LocalDate date,
    Boolean isWorkDay,
    Boolean isHoliday,
    Instant createdAt
) {}