package com.wateracademy.controller;

import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.VenueFilterOptionsResponse;
import com.wateracademy.dto.response.VenueResponse;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.service.VenueService;
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
@RequestMapping("/api/workspaces/{workspaceId}/venues")
public class VenueController {

    private final VenueService service;

    public VenueController(VenueService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<VenueResponse>> findAll(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) CourseType type,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity) {
        return ResponseEntity.ok(service.findPageByWorkspaceId(
                workspaceId, page, size, sort, search, city, type, minCapacity, maxCapacity));
    }

    @GetMapping("/all")
    public ResponseEntity<List<VenueResponse>> findAllUnpaged(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<VenueFilterOptionsResponse> filterOptions(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.filterOptions(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<VenueResponse> create(@PathVariable Long workspaceId,
                                                 @RequestBody @Valid VenueRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/venues/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> update(@PathVariable Long id,
                                                 @RequestBody @Valid VenueRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
