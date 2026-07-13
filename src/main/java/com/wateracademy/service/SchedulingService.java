package com.wateracademy.service;

import com.wateracademy.dto.response.TaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SchedulingService {

    private static final Logger log = LoggerFactory.getLogger(SchedulingService.class);

    private final TaskService taskService;
    private final GaRunnerService gaRunnerService;

    public SchedulingService(TaskService taskService,
                             GaRunnerService gaRunnerService) {
        this.taskService = taskService;
        this.gaRunnerService = gaRunnerService;
    }

    public TaskResponse startScheduling(Long workspaceId, String mode) {
        log.info("Scheduling requested: workspaceId={}, mode={}", workspaceId, mode);
        var taskResponse = taskService.create(workspaceId, mode);

        gaRunnerService.runGaAsync(workspaceId, taskResponse.id(), mode);

        return taskResponse;
    }
}
