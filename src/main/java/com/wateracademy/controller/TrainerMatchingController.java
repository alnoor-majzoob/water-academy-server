package com.wateracademy.controller;

import com.wateracademy.dto.request.MatchingAssignRequest;
import com.wateracademy.dto.request.MatchingRecommendationRequest;
import com.wateracademy.dto.request.MatchingSaveTrainerRequest;
import com.wateracademy.dto.response.MatchingCoursePlanResponse;
import com.wateracademy.dto.response.MatchingProfileAnalysisResponse;
import com.wateracademy.dto.response.MatchingRecommendationResponse;
import com.wateracademy.dto.response.MatchingTrainerDto;
import com.wateracademy.service.TrainerMatchingService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/matching")
public class TrainerMatchingController {

    private final TrainerMatchingService service;

    public TrainerMatchingController(TrainerMatchingService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.getHealth());
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> settings(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.getSettings());
    }

    @PostMapping("/trainers/analyze")
    public ResponseEntity<MatchingProfileAnalysisResponse> analyzeCv(
            @PathVariable Long workspaceId,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("trainer_id") String trainerId,
            @RequestParam(value = "provider", defaultValue = "") String provider) {
        return ResponseEntity.ok(service.analyzeCv(cv, trainerId, provider));
    }

    @PostMapping("/trainers")
    public ResponseEntity<Map<String, Object>> saveTrainer(
            @PathVariable Long workspaceId,
            @RequestBody MatchingSaveTrainerRequest request) {
        return ResponseEntity.ok(service.saveTrainer(request));
    }

    @GetMapping("/trainers")
    public ResponseEntity<Map<String, Object>> listTrainers(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(Map.of("trainers", service.listTrainers()));
    }

    @GetMapping("/trainers/by-trainer-id/{trainerId}")
    public ResponseEntity<MatchingTrainerDto> getTrainer(
            @PathVariable Long workspaceId,
            @PathVariable String trainerId) {
        return ResponseEntity.ok(service.getTrainer(trainerId));
    }

    @DeleteMapping("/trainers/{trainerId}")
    public ResponseEntity<Map<String, Object>> deleteTrainer(
            @PathVariable Long workspaceId,
            @PathVariable int trainerId) {
        return ResponseEntity.ok(service.deleteTrainer(trainerId));
    }

    @PostMapping("/recommendations")
    public ResponseEntity<MatchingRecommendationResponse> recommendations(
            @PathVariable Long workspaceId,
            @RequestBody MatchingRecommendationRequest request) {
        return ResponseEntity.ok(service.getRecommendations(request));
    }

    @PostMapping("/course-plans/{planId}/assign")
    public ResponseEntity<MatchingCoursePlanResponse> assignTrainer(
            @PathVariable Long workspaceId,
            @PathVariable int planId,
            @RequestBody MatchingAssignRequest request) {
        return ResponseEntity.ok(service.assignTrainer(planId, request));
    }

    @GetMapping("/course-plans")
    public ResponseEntity<Map<String, Object>> listCoursePlans(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(Map.of("plans", service.listCoursePlans()));
    }
}
