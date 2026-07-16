package com.wateracademy.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MatchingRecommendationResponse(
    boolean ok,
    int planId,
    List<MatchingRecommendedTrainerDto> recommendedTrainers,
    Map<String, Object> proposal,
    Map<String, Object> matching
) {}
