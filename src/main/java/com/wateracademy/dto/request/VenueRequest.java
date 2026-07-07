package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.CourseType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record VenueRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 100) String city,
    @NotNull @Min(1) @Max(100000) Integer capacity,
    @NotNull CourseType type,
    LocalDate availableFrom,
    LocalDate availableTo,
    @Size(max = 5000) String unavailableDates,
    @Size(max = 5000) String equipmentNotes
) {}