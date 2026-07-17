package com.wateracademy.dto.response;

import java.util.List;
import java.util.Map;

public record MatchingCoursePlanResponse(
    boolean ok,
    MatchingCoursePlanDto plan
) {
    public record MatchingCoursePlanDto(
        int id,
        String courseName,
        String courseDesc,
        int attendees,
        Integer proposedTrainerId,
        Integer assignedTrainerId,
        Double matchScore,
        List<String> matchReasons,
        String status,
        String createdAt,
        String trainerName,
        String trainerNumber,
        Map<String, Object> trainerProfile
    ) {}
}
