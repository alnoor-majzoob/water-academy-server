package com.wateracademy.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MatchingProfileAnalysisResponse(
    Map<String, Object> profile,
    String cvText,
    String cvFilename,
    Map<String, Object> ai
) {}
