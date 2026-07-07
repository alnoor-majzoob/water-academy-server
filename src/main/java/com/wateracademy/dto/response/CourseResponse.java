package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.CourseType;
import java.time.Instant;
import java.time.LocalDate;

public record CourseResponse(
    Long id,
    Long workspaceId,
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
    Instant createdAt,
    Instant updatedAt,
    Long version
) {}