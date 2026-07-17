package com.wateracademy.service;

import com.wateracademy.dto.request.MatchingAssignRequest;
import com.wateracademy.dto.request.MatchingRecommendationRequest;
import com.wateracademy.dto.request.MatchingSaveTrainerRequest;
import com.wateracademy.dto.response.MatchingCoursePlanResponse;
import com.wateracademy.dto.response.MatchingProfileAnalysisResponse;
import com.wateracademy.dto.response.MatchingRecommendationResponse;
import com.wateracademy.dto.response.MatchingTrainerDto;
import com.wateracademy.exception.ExternalServiceException;
import com.wateracademy.repository.TrainerRepository;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TrainerMatchingService {

    private final RestClient client;
    private final TrainerRepository trainerRepository;

    public TrainerMatchingService(RestClient trainerMatchingClient, TrainerRepository trainerRepository) {
        this.client = trainerMatchingClient;
        this.trainerRepository = trainerRepository;
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

    @Transactional
    public Map<String, Object> saveTrainer(MatchingSaveTrainerRequest request) {
        var result = proxyPost("/api/trainers", request);
        try {
            var id = Long.parseLong(request.trainerId());
            trainerRepository.findById(id).ifPresent(t -> {
                t.setCvAnalyzed(true);
                trainerRepository.save(t);
            });
        } catch (NumberFormatException ignored) {
        }
        return result;
    }

    public MatchingTrainerDto getTrainer(String trainerId) {
        try {
            return client.get()
                .uri("/api/trainers/{trainerId}", trainerId)
                .retrieve()
                .body(MatchingTrainerDto.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    public List<MatchingTrainerDto> listTrainers() {
        try {
            record TrainersWrapper(List<MatchingTrainerDto> trainers) {}
            var wrapper = client.get()
                .uri("/api/trainers")
                .retrieve()
                .body(TrainersWrapper.class);
            return wrapper != null ? wrapper.trainers() : List.of();
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    public Map<String, Object> deleteTrainer(int trainerId) {
        try {
            return client.delete()
                .uri("/api/trainers/{trainerId}", trainerId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    public MatchingRecommendationResponse getRecommendations(MatchingRecommendationRequest request) {
        try {
            return client.post()
                .uri("/api/recommendations")
                .body(request)
                .retrieve()
                .body(MatchingRecommendationResponse.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    public MatchingCoursePlanResponse assignTrainer(int planId, MatchingAssignRequest request) {
        try {
            return client.post()
                .uri("/api/course-plans/{planId}/assign", planId)
                .body(request)
                .retrieve()
                .body(MatchingCoursePlanResponse.class);
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    public List<MatchingCoursePlanResponse.MatchingCoursePlanDto> listCoursePlans() {
        try {
            record PlansWrapper(List<MatchingCoursePlanResponse.MatchingCoursePlanDto> plans) {}
            var wrapper = client.get()
                .uri("/api/course-plans")
                .retrieve()
                .body(PlansWrapper.class);
            return wrapper != null ? wrapper.plans() : List.of();
        } catch (RestClientResponseException e) {
            throw new ExternalServiceException(
                HttpStatusCode.valueOf(e.getStatusCode().value()), e.getResponseBodyAsString());
        }
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
}
