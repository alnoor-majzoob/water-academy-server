package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.CourseType;
import java.time.LocalDate;

public record CourseRequest(
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
    String color
) {}