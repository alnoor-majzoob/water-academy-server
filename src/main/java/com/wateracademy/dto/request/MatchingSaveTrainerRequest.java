package com.wateracademy.dto.request;

import java.util.Map;

public record MatchingSaveTrainerRequest(
    String trainerId,
    Map<String, Object> profile,
    String cvText,
    String cvFilename
) {}
