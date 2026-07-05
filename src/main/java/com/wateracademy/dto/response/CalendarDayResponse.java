package com.wateracademy.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CalendarDayResponse(
    Long id,
    Long workspaceId,
    LocalDate date,
    Boolean isWorkDay,
    Boolean isHoliday,
    LocalDateTime createdAt
) {}
