package com.wateracademy.dto.request;

import java.util.List;

public record MatchingAssignRequest(
    int trainerId,
    Double score,
    List<String> reasons
) {}
