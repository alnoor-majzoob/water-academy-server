package com.wateracademy.repository;

import com.wateracademy.entity.Course;
import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.Trainer;
import com.wateracademy.entity.Venue;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ScheduleEntryRepositoryTest {

    @Autowired
    private ScheduleEntryRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    private Course persistCourse(Workspace workspace, String name) {
        var course = new Course();
        course.setWorkspace(workspace);
        course.setName(name);
        course.setDurationDays(3);
        course.setType(CourseType.IN_PERSON);
        return em.persist(course);
    }

    private Trainer persistTrainer(Workspace workspace, String name) {
        var trainer = new Trainer();
        trainer.setWorkspace(workspace);
        trainer.setName(name);
        return em.persist(trainer);
    }

    private Venue persistVenue(Workspace workspace, String name) {
        var venue = new Venue();
        venue.setWorkspace(workspace);
        venue.setName(name);
        venue.setCapacity(30);
        venue.setType(CourseType.IN_PERSON);
        return em.persist(venue);
    }

    @Test
    void shouldSaveScheduleEntry() {
        var workspace = persistWorkspace();
        var course = persistCourse(workspace, "Water Treatment");
        var trainer = persistTrainer(workspace, "Dr. Ahmed");
        var venue = persistVenue(workspace, "Main Hall");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course);
        entry.setTrainer(trainer);
        entry.setVenue(venue);
        entry.setStartDate(LocalDate.of(2026, 7, 1));
        entry.setEndDate(LocalDate.of(2026, 7, 3));
        entry.setStatus(ScheduleStatus.SCHEDULED);

        var saved = repository.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(saved.getEndDate()).isEqualTo(LocalDate.of(2026, 7, 3));
        assertThat(saved.getStatus()).isEqualTo(ScheduleStatus.SCHEDULED);
    }

    @Test
    void shouldFindByWorkspaceId() {
        var workspace = persistWorkspace();
        var course = persistCourse(workspace, "Piping");
        var trainer = persistTrainer(workspace, "Dr. Sara");
        var venue = persistVenue(workspace, "Room A");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course);
        entry.setTrainer(trainer);
        entry.setVenue(venue);
        entry.setStartDate(LocalDate.of(2026, 7, 5));
        entry.setEndDate(LocalDate.of(2026, 7, 7));
        entry.setStatus(ScheduleStatus.SCHEDULED);
        em.persist(entry);

        List<ScheduleEntry> results = repository.findByWorkspaceId(workspace.getId());

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldDetectVenueConflict() {
        var workspace = persistWorkspace();
        var course1 = persistCourse(workspace, "Course A");
        var course2 = persistCourse(workspace, "Course B");
        var trainer1 = persistTrainer(workspace, "Trainer A");
        var trainer2 = persistTrainer(workspace, "Trainer B");
        var venue = persistVenue(workspace, "Shared Hall");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course1);
        entry.setTrainer(trainer1);
        entry.setVenue(venue);
        entry.setStartDate(LocalDate.of(2026, 7, 1));
        entry.setEndDate(LocalDate.of(2026, 7, 5));
        em.persist(entry);

        List<ScheduleEntry> conflicts = repository.findVenueConflicts(
                workspace.getId(), venue.getId(),
                LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7));

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getCourse().getName()).isEqualTo("Course A");
    }

    @Test
    void shouldNotDetectVenueConflictWhenDatesDontOverlap() {
        var workspace = persistWorkspace();
        var course = persistCourse(workspace, "Course A");
        var trainer = persistTrainer(workspace, "Trainer A");
        var venue = persistVenue(workspace, "Hall");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course);
        entry.setTrainer(trainer);
        entry.setVenue(venue);
        entry.setStartDate(LocalDate.of(2026, 7, 1));
        entry.setEndDate(LocalDate.of(2026, 7, 5));
        em.persist(entry);

        List<ScheduleEntry> conflicts = repository.findVenueConflicts(
                workspace.getId(), venue.getId(),
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 10));

        assertThat(conflicts).isEmpty();
    }

    @Test
    void shouldDetectTrainerConflict() {
        var workspace = persistWorkspace();
        var course1 = persistCourse(workspace, "Course A");
        var course2 = persistCourse(workspace, "Course B");
        var trainer = persistTrainer(workspace, "Busy Trainer");
        var venue1 = persistVenue(workspace, "Hall A");
        var venue2 = persistVenue(workspace, "Hall B");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course1);
        entry.setTrainer(trainer);
        entry.setVenue(venue1);
        entry.setStartDate(LocalDate.of(2026, 7, 1));
        entry.setEndDate(LocalDate.of(2026, 7, 5));
        em.persist(entry);

        List<ScheduleEntry> conflicts = repository.findTrainerConflicts(
                workspace.getId(), trainer.getId(),
                LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7));

        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getCourse().getName()).isEqualTo("Course A");
    }

    @Test
    void shouldAllowOnlineCourseWithoutVenue() {
        var workspace = persistWorkspace();
        var course = persistCourse(workspace, "Online Course");
        var trainer = persistTrainer(workspace, "Trainer A");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course);
        entry.setTrainer(trainer);
        entry.setVenue(null);
        entry.setStartDate(LocalDate.of(2026, 7, 1));
        entry.setEndDate(LocalDate.of(2026, 7, 3));
        entry.setStatus(ScheduleStatus.SCHEDULED);

        var saved = repository.save(entry);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVenue()).isNull();
    }

    @Test
    void shouldTrackConflictNotes() {
        var workspace = persistWorkspace();
        var course = persistCourse(workspace, "Course");
        var trainer = persistTrainer(workspace, "Trainer");
        var venue = persistVenue(workspace, "Venue");

        var entry = new ScheduleEntry();
        entry.setWorkspace(workspace);
        entry.setCourse(course);
        entry.setTrainer(trainer);
        entry.setVenue(venue);
        entry.setStartDate(LocalDate.of(2026, 7, 1));
        entry.setEndDate(LocalDate.of(2026, 7, 3));
        entry.setStatus(ScheduleStatus.SCHEDULED);
        entry.setConflictNotes("Trainer already booked on 2026-07-02");

        var saved = repository.save(entry);

        assertThat(saved.getConflictNotes()).contains("Trainer already booked");
    }
}
