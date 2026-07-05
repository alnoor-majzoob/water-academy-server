package com.wateracademy.repository;

import com.wateracademy.entity.CourseAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
    List<CourseAssignment> findByWorkspaceId(Long workspaceId);
    List<CourseAssignment> findByTrainerId(Long trainerId);
    List<CourseAssignment> findByCourseId(Long courseId);
}
