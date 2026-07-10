package com.wateracademy.controller;

import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.response.CourseFilterOptionsResponse;
import com.wateracademy.dto.response.CourseResponse;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.service.CourseService;
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
@RequestMapping("/api/workspaces/{workspaceId}/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CourseResponse>> findAll(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) List<String> sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourseType type,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String specialization) {
        return ResponseEntity.ok(service.findPageByWorkspaceId(
                workspaceId, page, size, sort, search, type, priority, city, specialization));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseResponse>> findAllUnpaged(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<CourseFilterOptionsResponse> filterOptions(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.filterOptions(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@PathVariable Long workspaceId,
                                                  @RequestBody @Valid CourseRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/courses/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable Long id,
                                                  @RequestBody @Valid CourseRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
