package com.wateracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record MatchingRecommendationResponse(
    boolean ok,
    @JsonProperty("plan_id") int planId,
    @JsonProperty("recommended_trainers") List<MatchingRecommendedTrainerDto> recommendedTrainers,
    Map<String, Object> proposal,
    Map<String, Object> matching
) {}
