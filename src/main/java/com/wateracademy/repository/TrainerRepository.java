package com.wateracademy.repository;

import com.wateracademy.entity.Trainer;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Trainer> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("""
            SELECT t FROM Trainer t
            WHERE t.workspace.id = :workspaceId
              AND (:search IS NULL OR LOWER(t.name) LIKE :search OR LOWER(t.externalId) LIKE :search OR LOWER(t.specialties) LIKE :search)
              AND (:city IS NULL OR t.city = :city)
              AND (:trainerType IS NULL OR t.trainerType = :trainerType)
              AND (:specialty IS NULL OR LOWER(t.specialties) LIKE :specialty)
            """)
    Page<Trainer> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                       @Param("search") String search,
                                       @Param("city") String city,
                                       @Param("trainerType") String trainerType,
                                       @Param("specialty") String specialty,
                                       Pageable pageable);

    @Query("SELECT DISTINCT t.city FROM Trainer t WHERE t.workspace.id = :workspaceId AND t.city IS NOT NULL AND t.city <> '' ORDER BY t.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT t.trainerType FROM Trainer t WHERE t.workspace.id = :workspaceId AND t.trainerType IS NOT NULL AND t.trainerType <> '' ORDER BY t.trainerType")
    List<String> findDistinctTrainerTypes(@Param("workspaceId") Long workspaceId);
}
