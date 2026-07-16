package com.wateracademy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record MatchingRecommendedTrainerDto(
    @JsonProperty("trainer_id") int trainerId,
    @JsonProperty("trainer_number") String trainerNumber,
    @JsonProperty("trainer_name") String trainerName,
    @JsonProperty("job_title") String jobTitle,
    double score,
    @JsonProperty("local_score") Double localScore,
    @JsonProperty("ai_score") Double aiScore,
    @JsonProperty("match_method") String matchMethod,
    @JsonProperty("fit_level") String fitLevel,
    List<String> reasons,
    List<String> risks,
    List<String> topics
) {}
