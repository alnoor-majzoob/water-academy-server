package com.wateracademy.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ScheduleEntryRequest(
    @NotNull Long courseId,
    @NotNull Long trainerId,
    Long venueId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    @Size(max = 5000) String conflictNotes
) {}
