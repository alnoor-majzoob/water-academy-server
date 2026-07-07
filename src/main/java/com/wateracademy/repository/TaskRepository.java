package com.wateracademy.repository;

import com.wateracademy.entity.Task;
import com.wateracademy.entity.enums.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Task> findByWorkspaceId(Long workspaceId);
    Optional<Task> findByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);
    boolean existsByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);
}
