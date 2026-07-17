package com.wateracademy.dto.response;

import java.util.Map;

public record MatchingTrainerDto(
    int id,
    String trainerId,
    String fullName,
    Map<String, Object> profile,
    String cvText,
    String cvFilename,
    String createdAt
) {}
