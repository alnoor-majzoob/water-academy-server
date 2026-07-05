package com.wateracademy.controller;

import com.wateracademy.dto.request.CalendarDayRequest;
import com.wateracademy.dto.response.CalendarDayResponse;
import com.wateracademy.service.CalendarDayService;
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
@RequestMapping("/api/workspaces/{workspaceId}/calendar-days")
public class CalendarDayController {

    private final CalendarDayService service;

    public CalendarDayController(CalendarDayService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CalendarDayResponse>> findAll(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.findAllByWorkspaceId(workspaceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarDayResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<CalendarDayResponse> create(@PathVariable Long workspaceId,
                                                       @RequestBody @Valid CalendarDayRequest request) {
        var response = service.create(workspaceId, request);
        return ResponseEntity.created(URI.create("/api/workspaces/" + workspaceId + "/calendar-days/" + response.id())).body(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<CalendarDayResponse>> bulkCreate(@PathVariable Long workspaceId,
                                                                 @RequestBody @Valid List<CalendarDayRequest> requests) {
        var responses = service.bulkCreate(workspaceId, requests);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalendarDayResponse> update(@PathVariable Long id,
                                                       @RequestBody @Valid CalendarDayRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}