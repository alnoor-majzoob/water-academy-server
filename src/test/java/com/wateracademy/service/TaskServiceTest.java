package com.wateracademy.service;

import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.TaskStatus;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.exception.TaskAlreadyRunningException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private WorkspaceService workspaceService;

    private Long createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    @Test
    void create_shouldPersistPendingTask() {
        var wsId = createWorkspace("Create Task");
        var response = taskService.create(wsId);
        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void create_shouldRejectWhenRunningExists() {
        var wsId = createWorkspace("Dup Task");
        var task = taskService.create(wsId);
        taskService.start(task.id());
        assertThatThrownBy(() -> taskService.create(wsId))
                .isInstanceOf(TaskAlreadyRunningException.class);
    }

    @Test
    void create_shouldAllowNewTaskAfterPreviousCompleted() {
        var wsId = createWorkspace("Sequential");
        var t1 = taskService.create(wsId);
        taskService.start(t1.id());
        taskService.complete(t1.id(), "Done");

        var t2 = taskService.create(wsId);
        assertThat(t2.id()).isNotNull();
    }

    @Test
    void start_shouldSetRunning() {
        var wsId = createWorkspace("Start");
        var created = taskService.create(wsId);
        var started = taskService.start(created.id());
        assertThat(started.status()).isEqualTo(TaskStatus.RUNNING);
    }

    @Test
    void complete_shouldSetCompletedAndLog() {
        var wsId = createWorkspace("Complete");
        var task = taskService.create(wsId);
        taskService.start(task.id());
        var completed = taskService.complete(task.id(), "All done successfully");
        assertThat(completed.status()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(completed.log()).isEqualTo("All done successfully");
    }

    @Test
    void fail_shouldSetFailedAndLog() {
        var wsId = createWorkspace("Fail");
        var task = taskService.create(wsId);
        taskService.start(task.id());
        var failed = taskService.fail(task.id(), "Error: timeout");
        assertThat(failed.status()).isEqualTo(TaskStatus.FAILED);
        assertThat(failed.log()).isEqualTo("Error: timeout");
    }

    @Test
    void findAllByWorkspaceId_shouldReturnTasks() {
        var wsId = createWorkspace("List Tasks");
        taskService.create(wsId);
        taskService.create(wsId);
        assertThat(taskService.findAllByWorkspaceId(wsId)).hasSize(2);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> taskService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}