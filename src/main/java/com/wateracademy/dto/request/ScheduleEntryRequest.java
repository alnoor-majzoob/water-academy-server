package com.wateracademy.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ScheduleEntryRequest(
    @NotNull UUID courseId,
    @NotNull UUID trainerId,
    UUID venueId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate,
    String conflictNotes
) {}
