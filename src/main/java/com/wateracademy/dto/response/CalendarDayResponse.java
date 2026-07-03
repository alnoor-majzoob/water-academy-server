package com.wateracademy.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CalendarDayResponse(
    UUID id,
    UUID workspaceId,
    LocalDate date,
    Boolean isWorkDay,
    Boolean isHoliday,
    LocalDateTime createdAt
) {}
