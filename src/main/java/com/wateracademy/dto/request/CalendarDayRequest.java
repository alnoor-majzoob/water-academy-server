package com.wateracademy.dto.request;

import java.time.LocalDate;

public record CalendarDayRequest(
    LocalDate date,
    Boolean isWorkDay,
    Boolean isHoliday
) {}
