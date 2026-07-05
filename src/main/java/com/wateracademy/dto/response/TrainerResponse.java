package com.wateracademy.dto.response;

import java.time.LocalDateTime;

public record TrainerResponse(
    Long id,
    Long workspaceId,
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