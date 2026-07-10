package com.wateracademy.controller;

import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.request.ScheduleEntryStatusRequest;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.ScheduleEntryFilterOptionsResponse;
import com.wateracademy.dto.response.ScheduleEntryResponse;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.service.ScheduleEntryService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
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
    public ResponseEntity<PageResponse<ScheduleEntryResponse>> findAll(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Boolean hasConflict) {
        return ResponseEntity.ok(service.findPageByWorkspaceId(
                workspaceId, page, size, sort, status, city, month, from, to, trainerId, venueId, courseId, hasConflict));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ScheduleEntryResponse>> findAllUnpaged(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ScheduleEntryFilterOptionsResponse> filterOptions(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.filterOptions(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleEntryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ScheduleEntryResponse> create(@PathVariable Long workspaceId,
                                                         @RequestBody @Valid ScheduleEntryRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/schedule-entries/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleEntryResponse> update(@PathVariable Long id,
                                                         @RequestBody @Valid ScheduleEntryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ScheduleEntryResponse> updateStatus(@PathVariable Long id,
                                                               @RequestBody @Valid ScheduleEntryStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conflicts/venue")
    public ResponseEntity<List<ScheduleEntryResponse>> findVenueConflicts(
            @PathVariable Long workspaceId,
            @RequestParam Long venueId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.findVenueConflicts(workspaceId, venueId, startDate, endDate));
    }

    @GetMapping("/conflicts/trainer")
    public ResponseEntity<List<ScheduleEntryResponse>> findTrainerConflicts(
            @PathVariable Long workspaceId,
            @RequestParam Long trainerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.findTrainerConflicts(workspaceId, trainerId, startDate, endDate));
    }
}
