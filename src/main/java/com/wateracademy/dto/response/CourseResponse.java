package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.CourseType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record CourseResponse(
    UUID id,
    UUID workspaceId,
    String externalId,
    String name,
    String specialization,
    Integer durationDays,
    Integer hoursPerDay,
    Integer expectedTrainees,
    String city,
    String beneficiary,
    String priority,
    CourseType type,
    LocalDate earliestStart,
    LocalDate latestEnd,
    LocalDate fixedDate,
    String notes,
    String color,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}