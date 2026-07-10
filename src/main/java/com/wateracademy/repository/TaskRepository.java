package com.wateracademy.repository;

import com.wateracademy.entity.Task;
import com.wateracademy.entity.enums.TaskStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Task> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("""
            SELECT t FROM Task t
            WHERE t.workspace.id = :workspaceId
              AND (:status IS NULL OR t.status = :status)
              AND (:type IS NULL OR t.mode = :type)
            """)
    Page<Task> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                    @Param("status") TaskStatus status,
                                    @Param("type") String type,
                                    Pageable pageable);

    Optional<Task> findByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);
    boolean existsByWorkspaceIdAndStatus(Long workspaceId, TaskStatus status);

    @Query("SELECT DISTINCT t.mode FROM Task t WHERE t.workspace.id = :workspaceId AND t.mode IS NOT NULL AND t.mode <> '' ORDER BY t.mode")
    List<String> findDistinctModes(@Param("workspaceId") Long workspaceId);
}
