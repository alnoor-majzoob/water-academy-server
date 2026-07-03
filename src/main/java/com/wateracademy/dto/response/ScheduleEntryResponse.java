package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleEntryResponse(
    UUID id,
    UUID workspaceId,
    UUID courseId,
    String courseName,
    UUID trainerId,
    String trainerName,
    UUID venueId,
    String venueName,
    LocalDate startDate,
    LocalDate endDate,
    ScheduleStatus status,
    String conflictNotes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
