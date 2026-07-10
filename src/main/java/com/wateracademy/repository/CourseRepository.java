package com.wateracademy.repository;

import com.wateracademy.entity.Course;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Course> findByWorkspaceId(Long workspaceId);

    long countByWorkspaceId(Long workspaceId);

    long countByWorkspaceIdAndType(Long workspaceId, CourseType type);

    @Query("SELECT c FROM Course c WHERE c.workspace.id = :workspaceId AND c.id NOT IN (SELECT s.course.id FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId)")
    List<Course> findUnscheduledByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT c.city FROM Course c WHERE c.workspace.id = :workspaceId AND c.city IS NOT NULL AND c.city <> '' ORDER BY c.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT c.priority FROM Course c WHERE c.workspace.id = :workspaceId AND c.priority IS NOT NULL AND c.priority <> '' ORDER BY c.priority")
    List<String> findDistinctPriorities(@Param("workspaceId") Long workspaceId);

    @Query("SELECT DISTINCT c.specialization FROM Course c WHERE c.workspace.id = :workspaceId AND c.specialization IS NOT NULL AND c.specialization <> '' ORDER BY c.specialization")
    List<String> findDistinctSpecializations(@Param("workspaceId") Long workspaceId);
}
