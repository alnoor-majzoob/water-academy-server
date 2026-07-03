package com.wateracademy.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TrainerResponse(
    UUID id,
    UUID workspaceId,
    String externalId,
    String name,
    String specialties,
    String city,
    String trainerType,
    String unavailableDates,
    Integer maxDaysPerMonth,
    Integer maxConsecutiveDays,
    Integer costPerDay,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}