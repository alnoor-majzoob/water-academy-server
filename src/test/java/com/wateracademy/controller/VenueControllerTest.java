package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.VenueRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VenueControllerTest {

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
    void findAll_shouldReturnEmptyList() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/venues", wsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var wsId = createWorkspace();
        var request = new VenueRequest("Main Hall", "Khartoum", 50, CourseType.IN_PERSON,
                null, null, null, "Projector");

        mockMvc.perform(post("/api/workspaces/{wsId}/venues", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Main Hall"))
                .andExpect(jsonPath("$.capacity").value(50));
    }

    @Test
    void findById_shouldReturnVenue() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/venues", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VenueRequest("Room A", null, 30, CourseType.IN_PERSON, null, null, null, null))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.VenueResponse.class);

        mockMvc.perform(get("/api/workspaces/{wsId}/venues/{id}", wsId, created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Room A"));
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/workspaces/{wsId}/venues/{id}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldModifyVenue() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/venues", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VenueRequest("Old", null, 10, CourseType.IN_PERSON, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.VenueResponse.class);

        var update = new VenueRequest("New Hall", "Riyadh", 100, CourseType.ONLINE,
                null, null, null, "Full AV");
        mockMvc.perform(put("/api/workspaces/{wsId}/venues/{id}", wsId, created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Hall"))
                .andExpect(jsonPath("$.type").value("ONLINE"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/venues", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VenueRequest("To Delete", null, 15, CourseType.IN_PERSON, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.VenueResponse.class);

        mockMvc.perform(delete("/api/workspaces/{wsId}/venues/{id}", wsId, created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workspaces/{wsId}/venues/{id}", wsId, created.id()))
                .andExpect(status().isNotFound());
    }
}