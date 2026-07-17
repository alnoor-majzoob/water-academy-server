package com.wateracademy.dto.response;

import java.util.List;

public record MatchingRecommendationResponse(
    boolean ok,
    int planId,
    List<MatchingRecommendedTrainerDto> recommendedTrainers,
    Proposal proposal,
    MatchingMeta matching
) {
    public record Proposal(Integer trainerId) {}

    public record MatchingMeta(
        boolean enabled,
        boolean used,
        String provider,
        String model,
        int durationMs,
        String error
    ) {}
}
