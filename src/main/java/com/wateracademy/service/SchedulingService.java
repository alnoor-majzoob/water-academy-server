package com.wateracademy.service;

import com.wateracademy.dto.response.TaskResponse;
import org.springframework.stereotype.Service;

@Service
public class SchedulingService {

    private final TaskService taskService;
    private final GaRunnerService gaRunnerService;

    public SchedulingService(TaskService taskService,
                             GaRunnerService gaRunnerService) {
        this.taskService = taskService;
        this.gaRunnerService = gaRunnerService;
    }

    public TaskResponse startScheduling(Long workspaceId, String mode) {
        var taskResponse = taskService.create(workspaceId, mode);

        gaRunnerService.runGaAsync(workspaceId, taskResponse.id(), mode);

        return taskResponse;
    }
}
