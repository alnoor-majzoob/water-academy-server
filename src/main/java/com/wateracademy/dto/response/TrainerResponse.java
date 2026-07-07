package com.wateracademy.dto.response;

import java.time.Instant;

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
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}