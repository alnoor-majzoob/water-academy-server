package com.wateracademy.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
