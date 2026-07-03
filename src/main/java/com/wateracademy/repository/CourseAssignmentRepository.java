package com.wateracademy.repository;

import com.wateracademy.entity.CourseAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, UUID> {
    List<CourseAssignment> findByWorkspaceId(UUID workspaceId);
    List<CourseAssignment> findByTrainerId(UUID trainerId);
    List<CourseAssignment> findByCourseId(UUID courseId);
}
