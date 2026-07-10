package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.Instant;
import java.time.LocalDate;

public record ScheduleEntryResponse(
    Long id,
    Long workspaceId,
    Long courseId,
    String courseName,
    Long trainerId,
    String trainerName,
    Long venueId,
    String venueName,
    String venueCity,
    LocalDate startDate,
    LocalDate endDate,
    ScheduleStatus status,
    String conflictNotes,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}
