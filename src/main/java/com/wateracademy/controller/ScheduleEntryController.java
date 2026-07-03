package com.wateracademy.controller;

import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.request.ScheduleEntryStatusRequest;
import com.wateracademy.dto.response.ScheduleEntryResponse;
import com.wateracademy.service.ScheduleEntryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/schedule-entries")
public class ScheduleEntryController {

    private final ScheduleEntryService service;

    public ScheduleEntryController(ScheduleEntryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleEntryResponse>> findAll(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleEntryResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ScheduleEntryResponse> create(@PathVariable UUID workspaceId,
                                                         @RequestBody @Valid ScheduleEntryRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/schedule-entries/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleEntryResponse> update(@PathVariable UUID id,
                                                         @RequestBody @Valid ScheduleEntryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ScheduleEntryResponse> updateStatus(@PathVariable UUID id,
                                                               @RequestBody @Valid ScheduleEntryStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conflicts/venue")
    public ResponseEntity<List<ScheduleEntryResponse>> findVenueConflicts(
            @PathVariable UUID workspaceId,
            @RequestParam UUID venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.findVenueConflicts(workspaceId, venueId, startDate, endDate));
    }

    @GetMapping("/conflicts/trainer")
    public ResponseEntity<List<ScheduleEntryResponse>> findTrainerConflicts(
            @PathVariable UUID workspaceId,
            @RequestParam UUID trainerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.findTrainerConflicts(workspaceId, trainerId, startDate, endDate));
    }
}