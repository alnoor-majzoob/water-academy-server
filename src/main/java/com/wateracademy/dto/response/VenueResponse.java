package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.CourseType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record VenueResponse(
    UUID id,
    UUID workspaceId,
    String externalId,
    String name,
    String city,
    Integer capacity,
    CourseType type,
    LocalDate availableFrom,
    LocalDate availableTo,
    String unavailableDates,
    String equipmentNotes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}