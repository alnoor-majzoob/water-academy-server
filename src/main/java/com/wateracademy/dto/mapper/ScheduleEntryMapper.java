package com.wateracademy.dto.mapper;

import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.response.ScheduleEntryResponse;
import com.wateracademy.entity.ScheduleEntry;
import org.springframework.stereotype.Component;

@Component
public class ScheduleEntryMapper {

    public ScheduleEntryResponse toResponse(ScheduleEntry entity) {
        return new ScheduleEntryResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getCourse().getId(),
            entity.getCourse().getName(),
            entity.getTrainer().getId(),
            entity.getTrainer().getName(),
            entity.getVenue() != null ? entity.getVenue().getId() : null,
            entity.getVenue() != null ? entity.getVenue().getName() : null,
            entity.getStartDate(),
            entity.getEndDate(),
            entity.getStatus(),
            entity.getConflictNotes(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public ScheduleEntry toEntity(ScheduleEntryRequest request) {
        var entry = new ScheduleEntry();
        entry.setStartDate(request.startDate());
        entry.setEndDate(request.endDate());
        entry.setConflictNotes(request.conflictNotes());
        return entry;
    }

    public void updateEntity(ScheduleEntry entity, ScheduleEntryRequest request) {
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setConflictNotes(request.conflictNotes());
    }
}
