package com.wateracademy.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ScheduleEntryRequest(
    @NotNull Long courseId,
    @NotNull Long trainerId,
    Long venueId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    String conflictNotes
) {}
