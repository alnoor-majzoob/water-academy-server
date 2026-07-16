package com.wateracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record MatchingProfileAnalysisResponse(
    Map<String, Object> profile,
    @JsonProperty("cv_text") String cvText,
    @JsonProperty("cv_filename") String cvFilename,
    Map<String, Object> ai
) {}
