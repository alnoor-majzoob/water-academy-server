package com.wateracademy.dto.mapper;

import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.response.VenueResponse;
import com.wateracademy.entity.Venue;
import org.springframework.stereotype.Component;

@Component
public class VenueMapper {

    public VenueResponse toResponse(Venue entity) {
        return new VenueResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getExternalId(),
            entity.getName(),
            entity.getCity(),
            entity.getCapacity(),
            entity.getType(),
            entity.getAvailableFrom(),
            entity.getAvailableTo(),
            entity.getUnavailableDates(),
            entity.getEquipmentNotes(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public Venue toEntity(VenueRequest request) {
        var venue = new Venue();
        updateEntity(venue, request);
        return venue;
    }

    public void updateEntity(Venue entity, VenueRequest request) {
        entity.setName(request.name());
        entity.setCity(request.city());
        entity.setCapacity(request.capacity());
        entity.setType(request.type());
        entity.setAvailableFrom(request.availableFrom());
        entity.setAvailableTo(request.availableTo());
        entity.setUnavailableDates(request.unavailableDates());
        entity.setEquipmentNotes(request.equipmentNotes());
    }
}