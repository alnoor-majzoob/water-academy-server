package com.wateracademy.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrainerRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 100) String city,
    @Size(max = 5000) String specialties,
    @Size(max = 20) String trainerType,
    @Size(max = 5000) String unavailableDates,
    @Min(1) @Max(31) Integer maxDaysPerMonth,
    @Min(1) @Max(31) Integer maxConsecutiveDays,
    @Min(0) Integer costPerDay,
    @Size(max = 5000) String notes
) {}