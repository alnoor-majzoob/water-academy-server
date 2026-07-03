package com.wateracademy.service;

import com.wateracademy.dto.mapper.TaskMapper;
import com.wateracademy.dto.response.TaskResponse;
import com.wateracademy.entity.Task;
import com.wateracademy.entity.enums.TaskStatus;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.exception.TaskAlreadyRunningException;
import com.wateracademy.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository repository;
    private final TaskMapper mapper;
    private final WorkspaceService workspaceService;

    public TaskService(TaskRepository repository, TaskMapper mapper,
                       WorkspaceService workspaceService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAllByWorkspaceId(UUID workspaceId) {
        return repository.findAll().stream()
                .filter(t -> t.getWorkspace().getId().equals(workspaceId))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        return mapper.toResponse(findEntity(id));
    }

    public TaskResponse create(UUID workspaceId) {
        var workspace = workspaceService.findEntity(workspaceId);

        if (repository.existsByWorkspaceIdAndStatus(workspaceId, TaskStatus.RUNNING)) {
            throw new TaskAlreadyRunningException(
                    "A task is already running for this workspace");
        }

        var task = new Task();
        task.setWorkspace(workspace);
        task.setStatus(TaskStatus.PENDING);
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse start(UUID id) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse complete(UUID id, String log) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setLog(log);
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse fail(UUID id, String errorLog) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(LocalDateTime.now());
        task.setLog(errorLog);
        return mapper.toResponse(repository.save(task));
    }

    public void delete(UUID id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Task findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
}
