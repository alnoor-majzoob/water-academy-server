package com.wateracademy.repository;

import com.wateracademy.entity.Trainer;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerRepository extends JpaRepository<Trainer, Long>, JpaSpecificationExecutor<Trainer> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Trainer> findByWorkspaceId(Long workspaceId);

    long countByWorkspaceId(Long workspaceId);

    @Query("SELECT DISTINCT t.city FROM Trainer t WHERE t.workspace.id = :workspaceId AND t.city IS NOT NULL AND t.city <> '' ORDER BY t.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT t.trainerType FROM Trainer t WHERE t.workspace.id = :workspaceId AND t.trainerType IS NOT NULL AND t.trainerType <> '' ORDER BY t.trainerType")
    List<String> findDistinctTrainerTypes(@Param("workspaceId") Long workspaceId);
}
