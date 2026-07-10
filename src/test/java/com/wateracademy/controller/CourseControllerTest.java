package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import java.time.LocalDate;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long createWorkspace() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("WS", null, 2026, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class).id();
    }

    @Test
    void findAll_shouldReturnEmptyList() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/courses", wsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var wsId = createWorkspace();
        var request = new CourseRequest("Water Treatment", "Engineering", 5, 6, 20,
                "Khartoum", "Ministry", "HIGH", CourseType.IN_PERSON,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), null, null, null);

        mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Water Treatment"))
                .andExpect(jsonPath("$.durationDays").value(5));
    }

    @Test
    void findById_shouldReturnCourse() throws Exception {
        var wsId = createWorkspace();
        var request = new CourseRequest("Safety", null, 2, null, null,
                null, null, null, CourseType.ONLINE, null, null, null, null, null);
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.CourseResponse.class);

        mockMvc.perform(get("/api/workspaces/{wsId}/courses/{id}", wsId, created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Safety"));
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/workspaces/{wsId}/courses/{id}", 999L, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldModifyCourse() throws Exception {
        var wsId = createWorkspace();
        var request = new CourseRequest("Old", null, 3, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null);
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.CourseResponse.class);

        var update = new CourseRequest("New Name", "Specialized", 5, 8, 25,
                "Riyadh", "HR", "LOW", CourseType.ONLINE,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), null, "Notes", "#FF0000");

        mockMvc.perform(put("/api/workspaces/{wsId}/courses/{id}", wsId, created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.type").value("ONLINE"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var wsId = createWorkspace();
        var request = new CourseRequest("To Delete", null, 1, null, null,
                null, null, null, CourseType.IN_PERSON, null, null, null, null, null);
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.CourseResponse.class);

        mockMvc.perform(delete("/api/workspaces/{wsId}/courses/{id}", wsId, created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workspaces/{wsId}/courses/{id}", wsId, created.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void workspaceIsolation_shouldNotMixCourses() throws Exception {
        var ws1 = createWorkspace();
        var ws2 = createWorkspace();

        mockMvc.perform(post("/api/workspaces/{wsId}/courses", ws1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseRequest("WS1 Course", null, 1, null, null,
                                        null, null, null, CourseType.IN_PERSON, null, null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workspaces/{wsId}/courses", ws2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
