package com.wateracademy.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MatchingRecommendationRequest(
    String courseName,
    String courseDesc,
    Integer attendees,
    Boolean useAiRerank,
    String aiProvider,
    Integer topK
) {}
