package com.wateracademy.service;

import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.dto.response.CourseResponse;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private WorkspaceService workspaceService;

    private Long createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    @Test
    void create_shouldPersistCourse() {
        var wsId = createWorkspace("Courses Test");
        var request = new CourseRequest("Water Treatment", "Engineering", 5, 6, 20,
                "Khartoum", "Ministry", "HIGH", CourseType.IN_PERSON,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), null,
                "Bring laptops", "#00FF00");
        var response = courseService.create(wsId, request);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Water Treatment");
        assertThat(response.durationDays()).isEqualTo(5);
        assertThat(response.type()).isEqualTo(CourseType.IN_PERSON);
        assertThat(response.priority()).isEqualTo("HIGH");
    }

    @Test
    void findById_shouldReturnCourse() {
        var wsId = createWorkspace("Find Course");
        var created = courseService.create(wsId, new CourseRequest("Safety", null, 2, null, null,
                null, null, null, CourseType.ONLINE, null, null, null, null, null));
        var found = courseService.findById(created.id());
        assertThat(found.name()).isEqualTo("Safety");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> courseService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllByWorkspaceId_shouldReturnScopedResults() {
        var ws1 = createWorkspace("WS1");
        var ws2 = createWorkspace("WS2");
        courseService.create(ws1, new CourseRequest("Course A", null, 1, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null));
        courseService.create(ws1, new CourseRequest("Course B", null, 1, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null));
        courseService.create(ws2, new CourseRequest("Course C", null, 1, null, null,
                null, null, null, CourseType.ONLINE, null, null, null, null, null));

        List<CourseResponse> ws1Courses = courseService.findAllByWorkspaceId(ws1);
        assertThat(ws1Courses).hasSize(2);
        assertThat(courseService.findAllByWorkspaceId(ws2)).hasSize(1);
    }

    @Test
    void update_shouldModifyFields() {
        var wsId = createWorkspace("Update Course");
        var created = courseService.create(wsId, new CourseRequest("Old Name", null, 3, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null));
        var updated = courseService.update(created.id(), new CourseRequest("New Name", "Specialized", 5, 8, 25,
                "Riyadh", "HR", "LOW", CourseType.ONLINE,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5),
                LocalDate.of(2026, 4, 1), "Updated notes", "#FF0000"));
        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.specialization()).isEqualTo("Specialized");
        assertThat(updated.durationDays()).isEqualTo(5);
        assertThat(updated.type()).isEqualTo(CourseType.ONLINE);
    }

    @Test
    void delete_shouldRemoveCourse() {
        var wsId = createWorkspace("Delete Course");
        var created = courseService.create(wsId, new CourseRequest("To Delete", null, 1, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null));
        courseService.delete(created.id());
        assertThatThrownBy(() -> courseService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}