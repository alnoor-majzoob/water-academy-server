package com.wateracademy.service;

import com.wateracademy.dto.mapper.TaskMapper;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.dto.response.TaskFilterOptionsResponse;
import com.wateracademy.dto.response.TaskResponse;
import com.wateracademy.entity.Task;
import com.wateracademy.entity.enums.TaskStatus;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.exception.TaskAlreadyRunningException;
import com.wateracademy.repository.TaskRepository;
import com.wateracademy.util.PaginationUtils;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);
    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "status", "mode", "startedAt", "completedAt", "createdAt", "updatedAt");

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
    public PageResponse<TaskResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                            List<String> sort, TaskStatus status, String type) {
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("createdAt").descending());
        return PageResponse.from(repository.searchByWorkspaceId(
                workspaceId, status, blankToNull(type), pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public TaskFilterOptionsResponse filterOptions(Long workspaceId) {
        return new TaskFilterOptionsResponse(List.of(TaskStatus.values()), repository.findDistinctModes(workspaceId));
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
        task.setStartedAt(Instant.now());
        log.info("Task started: id={}, workspaceId={}", id, task.getWorkspace().getId());
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse complete(Long id, String taskLog) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());
        task.setLog(taskLog);
        log.info("Task completed: id={}, workspaceId={}", id, task.getWorkspace().getId());
        return mapper.toResponse(repository.save(task));
    }

    public TaskResponse fail(Long id, String errorLog) {
        var task = findEntity(id);
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(Instant.now());
        task.setLog(errorLog);
        log.error("Task failed: id={}, workspaceId={}, error={}", id, task.getWorkspace().getId(), errorLog);
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

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
