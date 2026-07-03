package com.wateracademy.repository;

import com.wateracademy.entity.Task;
import com.wateracademy.entity.enums.TaskStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Optional<Task> findByWorkspaceIdAndStatus(UUID workspaceId, TaskStatus status);
    boolean existsByWorkspaceIdAndStatus(UUID workspaceId, TaskStatus status);
}
