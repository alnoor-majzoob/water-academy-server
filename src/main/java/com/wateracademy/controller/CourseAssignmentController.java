package com.wateracademy.controller;

import com.wateracademy.dto.request.CourseAssignmentRequest;
import com.wateracademy.dto.response.CourseAssignmentResponse;
import com.wateracademy.service.CourseAssignmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/assignments")
public class CourseAssignmentController {

    private final CourseAssignmentService service;

    public CourseAssignmentController(CourseAssignmentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CourseAssignmentResponse>> findAll(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseAssignmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CourseAssignmentResponse> create(@PathVariable UUID workspaceId,
                                                            @RequestBody @Valid CourseAssignmentRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/assignments/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}