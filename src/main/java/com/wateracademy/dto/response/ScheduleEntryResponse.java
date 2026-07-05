package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduleEntryResponse(
    Long id,
    Long workspaceId,
    Long courseId,
    String courseName,
    Long trainerId,
    String trainerName,
    Long venueId,
    String venueName,
    LocalDate startDate,
    LocalDate endDate,
    ScheduleStatus status,
    String conflictNotes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
