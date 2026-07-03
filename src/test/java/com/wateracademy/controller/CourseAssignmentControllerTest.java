package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.CourseAssignmentRequest;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID createWorkspace() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("WS", null, 2026, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class).id();
    }

    private UUID createTrainer(UUID wsId) throws Exception {
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/trainers", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerRequest("Trainer A", null, null, null, null, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.TrainerResponse.class).id();
    }

    private UUID createCourse(UUID wsId) throws Exception {
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseRequest("Course A", null, 1, null, null,
                                        null, null, null, CourseType.IN_PERSON, null, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.CourseResponse.class).id();
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var wsId = createWorkspace();
        var trainerId = createTrainer(wsId);
        var courseId = createCourse(wsId);
        var request = new CourseAssignmentRequest(trainerId, courseId);

        mockMvc.perform(post("/api/workspaces/{wsId}/assignments", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void create_shouldRejectDuplicateWith409() throws Exception {
        var wsId = createWorkspace();
        var trainerId = createTrainer(wsId);
        var courseId = createCourse(wsId);
        var request = new CourseAssignmentRequest(trainerId, courseId);

        mockMvc.perform(post("/api/workspaces/{wsId}/assignments", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/workspaces/{wsId}/assignments", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void findAll_shouldReturnAssignments() throws Exception {
        var wsId = createWorkspace();
        var trainerId = createTrainer(wsId);
        var courseId = createCourse(wsId);

        mockMvc.perform(post("/api/workspaces/{wsId}/assignments", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CourseAssignmentRequest(trainerId, courseId))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workspaces/{wsId}/assignments", wsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var wsId = createWorkspace();
        var trainerId = createTrainer(wsId);
        var courseId = createCourse(wsId);
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/assignments", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CourseAssignmentRequest(trainerId, courseId))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.CourseAssignmentResponse.class);

        mockMvc.perform(delete("/api/workspaces/{wsId}/assignments/{id}", wsId, created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workspaces/{wsId}/assignments/{id}", wsId, created.id()))
                .andExpect(status().isNotFound());
    }
}