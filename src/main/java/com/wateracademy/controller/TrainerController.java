package com.wateracademy.controller;

import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.TrainerFilterOptionsResponse;
import com.wateracademy.dto.response.TrainerResponse;
import com.wateracademy.service.TrainerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/workspaces/{workspaceId}/trainers")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<TrainerResponse>> findAll(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String trainerType,
            @RequestParam(required = false) String specialty) {
        return ResponseEntity.ok(service.findPageByWorkspaceId(
                workspaceId, page, size, sort, search, city, trainerType, specialty));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TrainerResponse>> findAllUnpaged(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<TrainerFilterOptionsResponse> filterOptions(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.filterOptions(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TrainerResponse> create(@PathVariable Long workspaceId,
                                                   @RequestBody @Valid TrainerRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/trainers/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainerResponse> update(@PathVariable Long id,
                                                   @RequestBody @Valid TrainerRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
