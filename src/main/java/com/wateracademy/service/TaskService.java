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
    public List<TaskResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public TaskResponse create(Long workspaceId, String mode) {
        var workspace = workspaceService.findEntity(workspaceId);

        if (repository.existsByWorkspaceIdAndStatus(workspaceId, TaskStatus.RUNNING)) {
            throw new TaskAlreadyRunningException(
                    "A task is already running for this workspace");
        }

        var task = new Task();
        task.setWorkspace(workspace);
        task.setStatus(TaskStatus.PENDING);
        task.setMode(mode);
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse start(Long id) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse complete(Long id, String log) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setLog(log);
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse fail(Long id, String errorLog) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(LocalDateTime.now());
        task.setLog(errorLog);
        return mapper.toResponse(repository.save(task));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    Task findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
}
