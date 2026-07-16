package com.wateracademy.dto.response;

import java.util.Map;

public record MatchingCoursePlanResponse(
    boolean ok,
    Map<String, Object> plan
) {}
