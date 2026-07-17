package com.wateracademy.dto.request;

public record MatchingRecommendationRequest(
    String courseName,
    String courseDesc,
    Integer attendees,
    Boolean useAiRerank,
    String aiProvider,
    Integer topK
) {}
