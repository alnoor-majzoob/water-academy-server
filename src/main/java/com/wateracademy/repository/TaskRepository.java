package com.wateracademy.repository;

import com.wateracademy.entity.Task;
import com.wateracademy.entity.enums.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Task> findByWorkspaceId(Long workspaceId);

    Optional<Task> findByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);
    boolean existsByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);

    @Query("SELECT DISTINCT t.mode FROM Task t WHERE t.workspace.id = :workspaceId AND t.mode IS NOT NULL AND t.mode <> '' ORDER BY t.mode")
    List<String> findDistinctModes(@Param("workspaceId") Long workspaceId);
}
