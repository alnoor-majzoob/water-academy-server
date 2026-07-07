package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.CourseType;
import java.time.Instant;
import java.time.LocalDate;

public record VenueResponse(
    Long id,
    Long workspaceId,
    String externalId,
    String name,
    String city,
    Integer capacity,
    CourseType type,
    LocalDate availableFrom,
    LocalDate availableTo,
    String unavailableDates,
    String equipmentNotes,
    Instant createdAt,
    Instant updatedAt
) {}