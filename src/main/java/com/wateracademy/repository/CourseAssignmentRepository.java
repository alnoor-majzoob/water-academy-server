package com.wateracademy.repository;

import com.wateracademy.entity.CourseAssignment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
    @EntityGraph(attributePaths = {"course", "trainer"})
    List<CourseAssignment> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"course", "trainer"})
    @Query("""
            SELECT a FROM CourseAssignment a
            WHERE a.workspace.id = :workspaceId
              AND (:courseId IS NULL OR a.course.id = :courseId)
              AND (:trainerId IS NULL OR a.trainer.id = :trainerId)
              AND (:search IS NULL OR LOWER(a.course.name) LIKE :search OR LOWER(a.trainer.name) LIKE :search)
            """)
    Page<CourseAssignment> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                                @Param("courseId") Long courseId,
                                                @Param("trainerId") Long trainerId,
                                                @Param("search") String search,
                                                Pageable pageable);

    @EntityGraph(attributePaths = {"course", "trainer"})
    List<CourseAssignment> findByTrainerId(Long trainerId);
    @EntityGraph(attributePaths = {"course", "trainer"})
    List<CourseAssignment> findByCourseId(Long courseId);
}
