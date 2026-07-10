package com.wateracademy.controller;

import com.wateracademy.dto.response.DashboardResponse;
import com.wateracademy.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(service.getDashboard(workspaceId));
    }
}
