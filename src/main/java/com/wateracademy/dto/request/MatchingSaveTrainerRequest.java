package com.wateracademy.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MatchingSaveTrainerRequest(
    String trainerId,
    String fullName,
    Map<String, Object> profile,
    String cvText,
    String cvFilename
) {}
