package com.wateracademy.dto.response;

import java.util.List;

public record MatchingRecommendedTrainerDto(
    int trainerId,
    String trainerNumber,
    String trainerName,
    String jobTitle,
    double score,
    Double localScore,
    Double aiScore,
    String matchMethod,
    String fitLevel,
    List<String> reasons,
    List<String> risks,
    List<String> topics
) {}
