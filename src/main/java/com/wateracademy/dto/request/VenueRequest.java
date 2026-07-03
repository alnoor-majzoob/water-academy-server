package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.CourseType;
import java.time.LocalDate;

public record VenueRequest(
    String name,
    String city,
    Integer capacity,
    CourseType type,
    LocalDate availableFrom,
    LocalDate availableTo,
    String unavailableDates,
    String equipmentNotes
) {}