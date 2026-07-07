package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.dto.request.WorkspaceStatusRequest;
import com.wateracademy.entity.enums.WorkspaceStatus;

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
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findAll_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/workspaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var request = new WorkspaceRequest("Test Workspace", "Desc", 2026, "#FF5733");
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Test Workspace"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void findById_shouldReturnWorkspace() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("Find", null, 2026, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class);

        mockMvc.perform(get("/api/workspaces/{id}", created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Find"));
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/workspaces/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldModifyWorkspace() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("Old", null, 2026, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class);

        var update = new WorkspaceRequest("Updated", "New desc", 2027, "#FFFFFF");
        mockMvc.perform(put("/api/workspaces/{id}", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.year").value(2027));
    }

    @Test
    void updateStatus_shouldChangeStatus() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("WS", null, 2026, null))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class);

        var statusReq = new WorkspaceStatusRequest(WorkspaceStatus.IMPORTED);
        mockMvc.perform(put("/api/workspaces/{id}/status", created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IMPORTED"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("Del", null, 2026, null))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class);

        mockMvc.perform(delete("/api/workspaces/{id}", created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workspaces/{id}", created.id()))
                .andExpect(status().isNotFound());
    }
}