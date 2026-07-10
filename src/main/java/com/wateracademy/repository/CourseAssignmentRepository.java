package com.wateracademy.repository;

import com.wateracademy.entity.CourseAssignment;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long>, JpaSpecificationExecutor<CourseAssignment> {
    @EntityGraph(attributePaths = {"course", "trainer"})
    List<CourseAssignment> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"course", "trainer"})
    List<CourseAssignment> findByTrainerId(Long trainerId);
    @EntityGraph(attributePaths = {"course", "trainer"})
    List<CourseAssignment> findByCourseId(Long courseId);
}
