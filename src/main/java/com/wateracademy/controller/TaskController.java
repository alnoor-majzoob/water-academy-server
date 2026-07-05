package com.wateracademy.controller;

import com.wateracademy.dto.response.TaskResponse;
import com.wateracademy.service.TaskService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> findAll(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@PathVariable Long workspaceId) {
        var response = service.create(workspaceId);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/tasks/" + response.id())).body(response);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<TaskResponse> start(@PathVariable Long id) {
        return ResponseEntity.ok(service.start(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> complete(@PathVariable Long id,
                                                  @RequestBody(required = false) String log) {
        return ResponseEntity.ok(service.complete(id, log));
    }

    @PostMapping("/{id}/fail")
    public ResponseEntity<TaskResponse> fail(@PathVariable Long id,
                                              @RequestBody(required = false) String errorLog) {
        return ResponseEntity.ok(service.fail(id, errorLog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}