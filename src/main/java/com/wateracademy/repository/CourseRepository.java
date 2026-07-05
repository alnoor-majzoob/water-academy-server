package com.wateracademy.repository;

import com.wateracademy.entity.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByWorkspaceId(Long workspaceId);

    @Query("SELECT c FROM Course c WHERE c.workspace.id = :workspaceId AND c.id NOT IN (SELECT s.course.id FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId)")
    List<Course> findUnscheduledByWorkspaceId(@Param("workspaceId") Long workspaceId);
}
