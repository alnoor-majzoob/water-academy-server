package com.wateracademy.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CalendarDayRequest(
    @NotNull LocalDate date,
    Boolean isWorkDay,
    Boolean isHoliday
) {}
