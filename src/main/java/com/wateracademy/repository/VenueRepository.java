package com.wateracademy.repository;

import com.wateracademy.entity.Venue;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenueRepository extends JpaRepository<Venue, Long>, JpaSpecificationExecutor<Venue> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Venue> findByWorkspaceId(Long workspaceId);

    long countByWorkspaceId(Long workspaceId);

    @Query("SELECT DISTINCT v.city FROM Venue v WHERE v.workspace.id = :workspaceId AND v.city IS NOT NULL AND v.city <> '' ORDER BY v.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);
}
