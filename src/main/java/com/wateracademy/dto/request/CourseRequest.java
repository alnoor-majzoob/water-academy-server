package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.CourseType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CourseRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 255) String specialization,
    @NotNull @Min(1) @Max(365) Integer durationDays,
    @Min(1) @Max(24) Integer hoursPerDay,
    @Min(1) @Max(10000) Integer expectedTrainees,
    @Size(max = 100) String city,
    @Size(max = 255) String beneficiary,
    @Size(max = 20) String priority,
    @NotNull CourseType type,
    LocalDate earliestStart,
    LocalDate latestEnd,
    LocalDate fixedDate,
    @Size(max = 5000) String notes,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color
) {}