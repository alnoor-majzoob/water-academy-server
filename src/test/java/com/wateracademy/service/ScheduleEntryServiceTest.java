package com.wateracademy.service;

import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.exception.InvalidStatusTransitionException;
import com.wateracademy.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ScheduleEntryServiceTest {

    @Autowired
    private ScheduleEntryService entryService;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private VenueService venueService;

    private UUID createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    private UUID createCourse(UUID wsId, String name) {
        return courseService.create(wsId, new CourseRequest(name, null, 1, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null)).id();
    }

    private UUID createTrainer(UUID wsId, String name) {
        return trainerService.create(wsId, new TrainerRequest(name, null, null, null, null, null, null, null, null)).id();
    }

    private UUID createVenue(UUID wsId, String name) {
        return venueService.create(wsId, new VenueRequest(name, null, 30, CourseType.IN_PERSON, null, null, null, null)).id();
    }

    @Test
    void create_shouldPersistEntry() {
        var wsId = createWorkspace("Create Entry");
        var courseId = createCourse(wsId, "Course A");
        var trainerId = createTrainer(wsId, "Trainer A");
        var venueId = createVenue(wsId, "Venue A");

        var response = entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, venueId,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), null));

        assertThat(response.id()).isNotNull();
        assertThat(response.status()).isEqualTo(ScheduleStatus.SCHEDULED);
        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 7, 3));
    }

    @Test
    void create_shouldDetectVenueConflict() {
        var wsId = createWorkspace("Venue Conflict");
        var course1 = createCourse(wsId, "Course 1");
        var course2 = createCourse(wsId, "Course 2");
        var trainer1 = createTrainer(wsId, "Trainer 1");
        var trainer2 = createTrainer(wsId, "Trainer 2");
        var venueId = createVenue(wsId, "Shared Hall");

        entryService.create(wsId, new ScheduleEntryRequest(
                course1, trainer1, venueId,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), null));

        var second = entryService.create(wsId, new ScheduleEntryRequest(
                course2, trainer2, venueId,
                LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7), null));

        assertThat(second.conflictNotes()).contains("Venue conflict");
    }

    @Test
    void create_shouldDetectTrainerConflict() {
        var wsId = createWorkspace("Trainer Conflict");
        var course1 = createCourse(wsId, "Course 1");
        var course2 = createCourse(wsId, "Course 2");
        var trainerId = createTrainer(wsId, "Busy Trainer");
        var venue1 = createVenue(wsId, "Venue 1");
        var venue2 = createVenue(wsId, "Venue 2");

        entryService.create(wsId, new ScheduleEntryRequest(
                course1, trainerId, venue1,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), null));

        var second = entryService.create(wsId, new ScheduleEntryRequest(
                course2, trainerId, venue2,
                LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7), null));

        assertThat(second.conflictNotes()).contains("Trainer conflict");
    }

    @Test
    void create_shouldAllowOnlineCourseWithoutVenue() {
        var wsId = createWorkspace("Online Entry");
        var courseId = createCourse(wsId, "Online Course");
        var trainerId = createTrainer(wsId, "Trainer A");

        var response = entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, null,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), null));

        assertThat(response.id()).isNotNull();
    }

    @Test
    void updateStatus_shouldTransitionFromScheduledToConfirmed() {
        var wsId = createWorkspace("Status");
        var courseId = createCourse(wsId, "C");
        var trainerId = createTrainer(wsId, "T");

        var created = entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, null,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), null));

        var updated = entryService.updateStatus(created.id(), ScheduleStatus.CONFIRMED);
        assertThat(updated.status()).isEqualTo(ScheduleStatus.CONFIRMED);
    }

    @Test
    void updateStatus_shouldTransitionConfirmedToCompleted() {
        var wsId = createWorkspace("Complete");
        var courseId = createCourse(wsId, "C");
        var trainerId = createTrainer(wsId, "T");

        var created = entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, null,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), null));
        entryService.updateStatus(created.id(), ScheduleStatus.CONFIRMED);
        var completed = entryService.updateStatus(created.id(), ScheduleStatus.COMPLETED);
        assertThat(completed.status()).isEqualTo(ScheduleStatus.COMPLETED);
    }

    @Test
    void updateStatus_shouldRejectChangeFromCompleted() {
        var wsId = createWorkspace("Reject");
        var courseId = createCourse(wsId, "C");
        var trainerId = createTrainer(wsId, "T");

        var created = entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, null,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), null));
        entryService.updateStatus(created.id(), ScheduleStatus.CONFIRMED);
        entryService.updateStatus(created.id(), ScheduleStatus.COMPLETED);

        assertThatThrownBy(() ->
                entryService.updateStatus(created.id(), ScheduleStatus.SCHEDULED))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void findVenueConflicts_shouldReturnConflicts() {
        var wsId = createWorkspace("Find Conflict");
        var course1 = createCourse(wsId, "C1");
        var course2 = createCourse(wsId, "C2");
        var trainer1 = createTrainer(wsId, "T1");
        var trainer2 = createTrainer(wsId, "T2");
        var venueId = createVenue(wsId, "V");

        entryService.create(wsId, new ScheduleEntryRequest(
                course1, trainer1, venueId,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), null));

        var conflicts = entryService.findVenueConflicts(wsId, venueId,
                LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 7));
        assertThat(conflicts).hasSize(1);
    }

    @Test
    void findVenueConflicts_shouldNotReturnNonOverlapping() {
        var wsId = createWorkspace("No Conflict");
        var courseId = createCourse(wsId, "C");
        var trainerId = createTrainer(wsId, "T");
        var venueId = createVenue(wsId, "V");

        entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, venueId,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), null));

        var conflicts = entryService.findVenueConflicts(wsId, venueId,
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 10));
        assertThat(conflicts).isEmpty();
    }

    @Test
    void delete_shouldRemoveEntry() {
        var wsId = createWorkspace("Delete Entry");
        var courseId = createCourse(wsId, "C");
        var trainerId = createTrainer(wsId, "T");

        var created = entryService.create(wsId, new ScheduleEntryRequest(
                courseId, trainerId, null,
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), null));
        entryService.delete(created.id());
        assertThatThrownBy(() -> entryService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> entryService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}