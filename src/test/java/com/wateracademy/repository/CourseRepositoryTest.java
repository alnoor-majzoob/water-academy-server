package com.wateracademy.repository;

import com.wateracademy.entity.Course;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CourseRepositoryTest {

    @Autowired
    private CourseRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    @Test
    void shouldSaveCourse() {
        var workspace = persistWorkspace();

        var course = new Course();
        course.setWorkspace(workspace);
        course.setName("Water Treatment");
        course.setSpecialization("Engineering");
        course.setDurationDays(5);
        course.setExpectedTrainees(20);
        course.setCity("Khartoum");
        course.setBeneficiary("Ministry");
        course.setPriority("HIGH");
        course.setType(CourseType.IN_PERSON);
        course.setColor("#00FF00");

        var saved = repository.save(course);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Water Treatment");
        assertThat(saved.getDurationDays()).isEqualTo(5);
        assertThat(saved.getType()).isEqualTo(CourseType.IN_PERSON);
    }

    @Test
    void shouldFindByWorkspaceId() {
        var workspace = persistWorkspace();

        var course = new Course();
        course.setWorkspace(workspace);
        course.setName("Piping Design");
        course.setDurationDays(3);
        course.setType(CourseType.IN_PERSON);
        em.persist(course);

        List<Course> results = repository.findByWorkspaceId(workspace.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Piping Design");
    }

    @Test
    void shouldHandleOnlineCourseWithoutVenue() {
        var workspace = persistWorkspace();

        var course = new Course();
        course.setWorkspace(workspace);
        course.setName("Online Safety");
        course.setDurationDays(2);
        course.setType(CourseType.ONLINE);

        var saved = repository.save(course);

        assertThat(saved.getType()).isEqualTo(CourseType.ONLINE);
    }

    @Test
    void shouldReturnEmptyForUnknownWorkspace() {
        var results = repository.findByWorkspaceId(999L);
        assertThat(results).isEmpty();
    }
}
