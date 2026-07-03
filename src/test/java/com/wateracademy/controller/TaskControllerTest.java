package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.WorkspaceRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTest {

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

    @Test
    void create_shouldReturn201() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void start_shouldSetRunning() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.TaskResponse.class);

        mockMvc.perform(post("/api/workspaces/{wsId}/tasks/{id}/start", wsId, created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    void complete_shouldSetCompletedAndLog() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.TaskResponse.class);
        mockMvc.perform(post("/api/workspaces/{wsId}/tasks/{id}/start", wsId, created.id()));

        mockMvc.perform(post("/api/workspaces/{wsId}/tasks/{id}/complete", wsId, created.id())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("All done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.log").value("All done"));
    }

    @Test
    void fail_shouldSetFailedAndLog() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.TaskResponse.class);
        mockMvc.perform(post("/api/workspaces/{wsId}/tasks/{id}/start", wsId, created.id()));

        mockMvc.perform(post("/api/workspaces/{wsId}/tasks/{id}/fail", wsId, created.id())
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Error: timeout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.log").value("Error: timeout"));
    }

    @Test
    void findAll_shouldReturnTasks() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId));
        mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId));

        mockMvc.perform(get("/api/workspaces/{wsId}/tasks", wsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void create_shouldRejectWhenRunningExists() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.TaskResponse.class);
        mockMvc.perform(post("/api/workspaces/{wsId}/tasks/{id}/start", wsId, created.id()));

        mockMvc.perform(post("/api/workspaces/{wsId}/tasks", wsId))
                .andExpect(status().isConflict());
    }
}