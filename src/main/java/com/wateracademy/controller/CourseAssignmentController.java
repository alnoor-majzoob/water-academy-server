package com.wateracademy.controller;

import com.wateracademy.dto.request.CourseAssignmentRequest;
import com.wateracademy.dto.response.CourseAssignmentResponse;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.service.CourseAssignmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/assignments")
public class CourseAssignmentController {

    private final CourseAssignmentService service;

    public CourseAssignmentController(CourseAssignmentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CourseAssignmentResponse>> findAll(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long trainerId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(service.findPageByWorkspaceId(
                workspaceId, page, size, sort, courseId, trainerId, search));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseAssignmentResponse>> findAllUnpaged(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseAssignmentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CourseAssignmentResponse> create(@PathVariable Long workspaceId,
                                                            @RequestBody @Valid CourseAssignmentRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/assignments/" + response.id())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
