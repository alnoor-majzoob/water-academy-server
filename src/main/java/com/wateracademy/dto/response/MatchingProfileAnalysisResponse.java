package com.wateracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.util.Map;

public record MatchingProfileAnalysisResponse(
    Map<String, Object> profile,
    @JsonProperty(value = "cv_text", access = Access.READ_ONLY) String cvText,
    @JsonProperty(value = "cv_filename", access = Access.READ_ONLY) String cvFilename,
    Map<String, Object> ai
) {}
