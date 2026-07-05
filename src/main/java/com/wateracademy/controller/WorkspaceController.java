package com.wateracademy.controller;

import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.dto.request.WorkspaceStatusRequest;
import com.wateracademy.dto.response.WorkspaceResponse;
import com.wateracademy.service.WorkspaceService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

    private final WorkspaceService service;

    public WorkspaceController(WorkspaceService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> create(@RequestBody @Valid WorkspaceRequest request) {
        var response = service.create(request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkspaceResponse> update(@PathVariable Long id,
                                                     @RequestBody @Valid WorkspaceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WorkspaceResponse> updateStatus(@PathVariable Long id,
                                                           @RequestBody @Valid WorkspaceStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}