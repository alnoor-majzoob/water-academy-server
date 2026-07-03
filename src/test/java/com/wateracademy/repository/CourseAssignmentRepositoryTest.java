package com.wateracademy.repository;

import com.wateracademy.entity.Course;
import com.wateracademy.entity.CourseAssignment;
import com.wateracademy.entity.Trainer;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CourseAssignmentRepositoryTest {

    @Autowired
    private CourseAssignmentRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    private Trainer persistTrainer(Workspace workspace) {
        var trainer = new Trainer();
        trainer.setWorkspace(workspace);
        trainer.setName("Dr. Ahmed");
        return em.persist(trainer);
    }

    private Course persistCourse(Workspace workspace) {
        var course = new Course();
        course.setWorkspace(workspace);
        course.setName("Water Treatment");
        course.setDurationDays(5);
        course.setType(CourseType.IN_PERSON);
        return em.persist(course);
    }

    @Test
    void shouldSaveAssignment() {
        var workspace = persistWorkspace();
        var trainer = persistTrainer(workspace);
        var course = persistCourse(workspace);

        var assignment = new CourseAssignment();
        assignment.setWorkspace(workspace);
        assignment.setTrainer(trainer);
        assignment.setCourse(course);

        var saved = repository.save(assignment);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindByTrainerId() {
        var workspace = persistWorkspace();
        var trainer = persistTrainer(workspace);
        var course = persistCourse(workspace);

        var assignment = new CourseAssignment();
        assignment.setWorkspace(workspace);
        assignment.setTrainer(trainer);
        assignment.setCourse(course);
        em.persist(assignment);

        List<CourseAssignment> results = repository.findByTrainerId(trainer.getId());

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldFindByCourseId() {
        var workspace = persistWorkspace();
        var trainer = persistTrainer(workspace);
        var course = persistCourse(workspace);

        var assignment = new CourseAssignment();
        assignment.setWorkspace(workspace);
        assignment.setTrainer(trainer);
        assignment.setCourse(course);
        em.persist(assignment);

        List<CourseAssignment> results = repository.findByCourseId(course.getId());

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldRejectDuplicateAssignment() {
        var workspace = persistWorkspace();
        var trainer = persistTrainer(workspace);
        var course = persistCourse(workspace);

        var assignment1 = new CourseAssignment();
        assignment1.setWorkspace(workspace);
        assignment1.setTrainer(trainer);
        assignment1.setCourse(course);
        em.persist(assignment1);

        var assignment2 = new CourseAssignment();
        assignment2.setWorkspace(workspace);
        assignment2.setTrainer(trainer);
        assignment2.setCourse(course);

        assertThatThrownBy(() -> repository.saveAndFlush(assignment2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
