package com.wateracademy.repository;

import com.wateracademy.entity.Course;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Course> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("""
            SELECT c FROM Course c
            WHERE c.workspace.id = :workspaceId
              AND (:search IS NULL OR LOWER(c.name) LIKE :search OR LOWER(c.externalId) LIKE :search OR LOWER(c.beneficiary) LIKE :search)
              AND (:type IS NULL OR c.type = :type)
              AND (:priority IS NULL OR c.priority = :priority)
              AND (:city IS NULL OR c.city = :city)
              AND (:specialization IS NULL OR c.specialization = :specialization)
            """)
    Page<Course> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                      @Param("search") String search,
                                      @Param("type") CourseType type,
                                      @Param("priority") String priority,
                                      @Param("city") String city,
                                      @Param("specialization") String specialization,
                                      Pageable pageable);

    @Query("SELECT c FROM Course c WHERE c.workspace.id = :workspaceId AND c.id NOT IN (SELECT s.course.id FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId)")
    List<Course> findUnscheduledByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT c.city FROM Course c WHERE c.workspace.id = :workspaceId AND c.city IS NOT NULL AND c.city <> '' ORDER BY c.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT c.priority FROM Course c WHERE c.workspace.id = :workspaceId AND c.priority IS NOT NULL AND c.priority <> '' ORDER BY c.priority")
    List<String> findDistinctPriorities(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT c.specialization FROM Course c WHERE c.workspace.id = :workspaceId AND c.specialization IS NOT NULL AND c.specialization <> '' ORDER BY c.specialization")
    List<String> findDistinctSpecializations(@Param("workspaceId") Long workspaceId);
}
