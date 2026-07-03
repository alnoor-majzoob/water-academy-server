package com.wateracademy.dto.request;

public record TrainerRequest(
    String name,
    String city,
    String specialties,
    String trainerType,
    String unavailableDates,
    Integer maxDaysPerMonth,
    Integer maxConsecutiveDays,
    Integer costPerDay,
    String notes
) {}