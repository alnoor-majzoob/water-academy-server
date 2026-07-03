package com.wateracademy.service;

import com.wateracademy.dto.request.CourseAssignmentRequest;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.exception.DuplicateResourceException;
import com.wateracademy.exception.ResourceNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseAssignmentServiceTest {

    @Autowired
    private CourseAssignmentService assignmentService;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private CourseService courseService;

    private UUID createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    private UUID createTrainer(UUID wsId, String name) {
        return trainerService.create(wsId, new TrainerRequest(name, null, null, null, null, null, null, null, null)).id();
    }

    private UUID createCourse(UUID wsId, String name) {
        return courseService.create(wsId, new CourseRequest(name, null, 1, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null)).id();
    }

    @Test
    void create_shouldPersistAssignment() {
        var wsId = createWorkspace("Assign Test");
        var trainerId = createTrainer(wsId, "Trainer A");
        var courseId = createCourse(wsId, "Course A");

        var response = assignmentService.create(wsId, new CourseAssignmentRequest(trainerId, courseId));
        assertThat(response.id()).isNotNull();
    }

    @Test
    void create_shouldRejectDuplicate() {
        var wsId = createWorkspace("Dup Assign");
        var trainerId = createTrainer(wsId, "Trainer A");
        var courseId = createCourse(wsId, "Course A");

        assignmentService.create(wsId, new CourseAssignmentRequest(trainerId, courseId));
        assertThatThrownBy(() ->
                assignmentService.create(wsId, new CourseAssignmentRequest(trainerId, courseId)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void create_shouldRejectCrossWorkspaceTrainer() {
        var ws1 = createWorkspace("WS1");
        var ws2 = createWorkspace("WS2");
        var trainerId = createTrainer(ws1, "Trainer Alien");
        var courseId = createCourse(ws2, "Course B");

        assertThatThrownBy(() ->
                assignmentService.create(ws2, new CourseAssignmentRequest(trainerId, courseId)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllByWorkspaceId_shouldReturnScopedResults() {
        var ws1 = createWorkspace("WS1");
        var ws2 = createWorkspace("WS2");
        var t1 = createTrainer(ws1, "T1");
        var c1 = createCourse(ws1, "C1");
        var t2 = createTrainer(ws2, "T2");
        var c2 = createCourse(ws2, "C2");

        assignmentService.create(ws1, new CourseAssignmentRequest(t1, c1));
        assignmentService.create(ws2, new CourseAssignmentRequest(t2, c2));

        assertThat(assignmentService.findAllByWorkspaceId(ws1)).hasSize(1);
        assertThat(assignmentService.findAllByWorkspaceId(ws2)).hasSize(1);
    }

    @Test
    void delete_shouldRemoveAssignment() {
        var wsId = createWorkspace("Delete Assign");
        var trainerId = createTrainer(wsId, "T");
        var courseId = createCourse(wsId, "C");

        var created = assignmentService.create(wsId, new CourseAssignmentRequest(trainerId, courseId));
        assignmentService.delete(created.id());
        assertThatThrownBy(() -> assignmentService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> assignmentService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}