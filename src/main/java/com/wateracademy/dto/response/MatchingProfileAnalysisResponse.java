package com.wateracademy.dto.response;

import java.util.Map;

public record MatchingProfileAnalysisResponse(
    Map<String, Object> profile,
    String cvText,
    String cvFilename,
    AiMeta ai
) {
    public record AiMeta(String provider, String model, int durationMs) {}
}
