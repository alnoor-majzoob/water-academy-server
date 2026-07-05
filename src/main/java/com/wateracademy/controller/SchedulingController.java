package com.wateracademy.controller;

import com.wateracademy.dto.response.TaskResponse;
import com.wateracademy.service.SchedulingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
public class SchedulingController {

    private final SchedulingService schedulingService;

    public SchedulingController(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<TaskResponse> startSchedule(
            @PathVariable Long workspaceId,
            @RequestParam(defaultValue = "new") String mode) {
        TaskResponse task = schedulingService.startScheduling(workspaceId, mode);
        return ResponseEntity.accepted().body(task);
    }
}
