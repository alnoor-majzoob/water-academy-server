package com.wateracademy.service;

import com.wateracademy.dto.request.MatchingAssignRequest;
import com.wateracademy.dto.request.MatchingRecommendationRequest;
import com.wateracademy.dto.request.MatchingSaveTrainerRequest;
import com.wateracademy.dto.response.MatchingCoursePlanResponse;
import com.wateracademy.dto.response.MatchingProfileAnalysisResponse;
import com.wateracademy.dto.response.MatchingRecommendationResponse;
import com.wateracademy.exception.ExternalServiceException;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrainerMatchingService {

    private final RestClient client;

    public TrainerMatchingService(RestClient trainerMatchingClient) {
        this.client = trainerMatchingClient;
    }

    public Map<String, Object> getHealth() {
        return proxyGet("/api/health");
    }

    public Map<String, Object> getSettings() {
        return proxyGet("/api/settings");
    }

    public MatchingProfileAnalysisResponse analyzeCv(MultipartFile file, String trainerId, String provider) {
        try {
            var parts = new LinkedMultiValueMap<String, Object>();
            parts.add("cv", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            parts.add("trainer_id", trainerId);
            parts.add("provider", provider == null ? "" : provider);

            return client.post()
                .uri("/api/trainers/analyze")
                .body(parts)
                .retrieve()
                .body(MatchingProfileAnalysisResponse.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new ExternalServiceException(HttpStatusCode.valueOf(502), e.getMessage());
        }
    }

    public Map<String, Object> saveTrainer(MatchingSaveTrainerRequest request) {
        return proxyPost("/api/trainers", request);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listTrainers() {
        var response = proxyGet("/api/trainers");
        return response;
    }

    public Map<String, Object> deleteTrainer(int trainerId) {
        try {
            return client.delete()
                .uri("/api/trainers/{trainerId}", trainerId)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    public MatchingRecommendationResponse getRecommendations(MatchingRecommendationRequest request) {
        return proxyPost("/api/recommendations", request, MatchingRecommendationResponse.class);
    }

    public MatchingCoursePlanResponse assignTrainer(int planId, MatchingAssignRequest request) {
        return proxyPost("/api/course-plans/{planId}/assign", request, MatchingCoursePlanResponse.class, planId);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> listCoursePlans() {
        return proxyGet("/api/course-plans");
    }

    private Map<String, Object> proxyGet(String path) {
        try {
            return client.get()
                .uri(path)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    private Map<String, Object> proxyPost(String path, Object request) {
        try {
            return client.post()
                .uri(path)
                .body(request)
                .retrieve()
                .body(Map.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    private <T> T proxyPost(String path, Object request, Class<T> responseType, Object... uriVars) {
        try {
            return client.post()
                .uri(path, uriVars)
                .body(request)
                .retrieve()
                .body(responseType);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }
}
