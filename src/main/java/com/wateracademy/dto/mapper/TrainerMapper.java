package com.wateracademy.dto.mapper;

import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.response.TrainerResponse;
import com.wateracademy.entity.Trainer;
import org.springframework.stereotype.Component;

@Component
public class TrainerMapper {

    public TrainerResponse toResponse(Trainer entity) {
        return new TrainerResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getExternalId(),
            entity.getName(),
            entity.getSpecialties(),
            entity.getCity(),
            entity.getTrainerType(),
            entity.getUnavailableDates(),
            entity.getMaxDaysPerMonth(),
            entity.getMaxConsecutiveDays(),
            entity.getCostPerDay(),
            entity.getNotes(),
            entity.getCvAnalyzed(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }

    public Trainer toEntity(TrainerRequest request) {
        var trainer = new Trainer();
        updateEntity(trainer, request);
        return trainer;
    }

    public void updateEntity(Trainer entity, TrainerRequest request) {
        entity.setName(request.name());
        entity.setCity(request.city());
        entity.setSpecialties(request.specialties());
        entity.setTrainerType(request.trainerType());
        entity.setUnavailableDates(request.unavailableDates());
        entity.setMaxDaysPerMonth(request.maxDaysPerMonth());
        entity.setMaxConsecutiveDays(request.maxConsecutiveDays());
        entity.setCostPerDay(request.costPerDay());
        entity.setNotes(request.notes());
    }
}