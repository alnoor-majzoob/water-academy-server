package com.wateracademy.repository;

import com.wateracademy.entity.Venue;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Venue> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("""
            SELECT v FROM Venue v
            WHERE v.workspace.id = :workspaceId
              AND (:search IS NULL OR LOWER(v.name) LIKE :search OR LOWER(v.externalId) LIKE :search OR LOWER(v.equipmentNotes) LIKE :search)
              AND (:city IS NULL OR v.city = :city)
              AND (:type IS NULL OR v.type = :type)
              AND (:minCapacity IS NULL OR v.capacity >= :minCapacity)
              AND (:maxCapacity IS NULL OR v.capacity <= :maxCapacity)
            """)
    Page<Venue> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                     @Param("search") String search,
                                     @Param("city") String city,
                                     @Param("type") CourseType type,
                                     @Param("minCapacity") Integer minCapacity,
                                     @Param("maxCapacity") Integer maxCapacity,
                                     Pageable pageable);

    @Query("SELECT DISTINCT v.city FROM Venue v WHERE v.workspace.id = :workspaceId AND v.city IS NOT NULL AND v.city <> '' ORDER BY v.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);
}
