package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.CalendarDayRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import java.time.LocalDate;
import java.util.List;
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
class CalendarDayControllerTest {

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
        var request = new CalendarDayRequest(LocalDate.of(2026, 7, 1), true, false);

        mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.date").value("2026-07-01"))
                .andExpect(jsonPath("$.isWorkDay").value(true));
    }

    @Test
    void create_shouldRejectDuplicateWith409() throws Exception {
        var wsId = createWorkspace();
        var request = new CalendarDayRequest(LocalDate.of(2026, 7, 1), true, false);

        mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void bulkCreate_shouldInsertMultipleDays() throws Exception {
        var wsId = createWorkspace();
        var requests = List.of(
                new CalendarDayRequest(LocalDate.of(2026, 1, 1), true, false),
                new CalendarDayRequest(LocalDate.of(2026, 1, 2), true, false),
                new CalendarDayRequest(LocalDate.of(2026, 1, 3), false, true));

        mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days/bulk", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void findAll_shouldReturnDays() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CalendarDayRequest(LocalDate.of(2026, 6, 1), true, false))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workspaces/{wsId}/calendar-days", wsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void update_shouldModifyDay() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CalendarDayRequest(LocalDate.of(2026, 7, 1), true, false))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.CalendarDayResponse.class);

        var update = new CalendarDayRequest(LocalDate.of(2026, 7, 4), false, true);
        mockMvc.perform(put("/api/workspaces/{wsId}/calendar-days/{id}", wsId, created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-07-04"))
                .andExpect(jsonPath("$.isWorkDay").value(false))
                .andExpect(jsonPath("$.isHoliday").value(true));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var wsId = createWorkspace();
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/calendar-days", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CalendarDayRequest(LocalDate.of(2026, 12, 25), false, true))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.CalendarDayResponse.class);

        mockMvc.perform(delete("/api/workspaces/{wsId}/calendar-days/{id}", wsId, created.id()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workspaces/{wsId}/calendar-days/{id}", wsId, created.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/workspaces/{wsId}/calendar-days/{id}", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}