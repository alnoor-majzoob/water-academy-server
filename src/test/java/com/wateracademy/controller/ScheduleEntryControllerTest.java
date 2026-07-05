package com.wateracademy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.ScheduleEntryRequest;
import com.wateracademy.dto.request.ScheduleEntryStatusRequest;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.entity.enums.ScheduleStatus;
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
class ScheduleEntryControllerTest {

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

    private Long createCourse(Long wsId) throws Exception {
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseRequest("Course", null, 1, null, null,
                                        null, null, null, CourseType.IN_PERSON, null, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.CourseResponse.class).id();
    }

    private Long createTrainer(Long wsId) throws Exception {
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/trainers", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TrainerRequest("Trainer", null, null, null, null, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.TrainerResponse.class).id();
    }

    private Long createVenue(Long wsId) throws Exception {
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/venues", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VenueRequest("Venue", null, 30, CourseType.IN_PERSON, null, null, null, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.VenueResponse.class).id();
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var wsId = createWorkspace();
        var courseId = createCourse(wsId);
        var trainerId = createTrainer(wsId);
        var venueId = createVenue(wsId);
        var request = new ScheduleEntryRequest(courseId, trainerId, venueId,
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3), null);

        mockMvc.perform(post("/api/workspaces/{wsId}/schedule-entries", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void updateStatus_shouldTransitionToConfirmed() throws Exception {
        var wsId = createWorkspace();
        var courseId = createCourse(wsId);
        var trainerId = createTrainer(wsId);
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/schedule-entries", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScheduleEntryRequest(courseId, trainerId, null,
                                        LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), null))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.ScheduleEntryResponse.class);

        var statusReq = new ScheduleEntryStatusRequest(ScheduleStatus.CONFIRMED);
        mockMvc.perform(put("/api/workspaces/{wsId}/schedule-entries/{id}/status", wsId, created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void findVenueConflicts_shouldReturnConflicts() throws Exception {
        var wsId = createWorkspace();
        var course1 = createCourse(wsId);
        var course2 = createCourse(wsId);
        var trainer1 = createTrainer(wsId);
        var trainer2 = createTrainer(wsId);
        var venueId = createVenue(wsId);

        mockMvc.perform(post("/api/workspaces/{wsId}/schedule-entries", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScheduleEntryRequest(course1, trainer1, venueId,
                                        LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workspaces/{wsId}/schedule-entries/conflicts/venue", wsId)
                        .param("venueId", venueId.toString())
                        .param("startDate", "2026-07-03")
                        .param("endDate", "2026-07-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findTrainerConflicts_shouldReturnConflicts() throws Exception {
        var wsId = createWorkspace();
        var course1 = createCourse(wsId);
        var course2 = createCourse(wsId);
        var trainerId = createTrainer(wsId);
        var venue1 = createVenue(wsId);
        var venue2 = createVenue(wsId);

        mockMvc.perform(post("/api/workspaces/{wsId}/schedule-entries", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScheduleEntryRequest(course1, trainerId, venue1,
                                        LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 5), null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/workspaces/{wsId}/schedule-entries/conflicts/trainer", wsId)
                        .param("trainerId", trainerId.toString())
                        .param("startDate", "2026-07-03")
                        .param("endDate", "2026-07-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        var wsId = createWorkspace();
        var courseId = createCourse(wsId);
        var trainerId = createTrainer(wsId);
        var json = mockMvc.perform(post("/api/workspaces/{wsId}/schedule-entries", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScheduleEntryRequest(courseId, trainerId, null,
                                        LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), null))))
                .andReturn().getResponse().getContentAsString();
        var created = objectMapper.readValue(json, com.wateracademy.dto.response.ScheduleEntryResponse.class);

        mockMvc.perform(delete("/api/workspaces/{wsId}/schedule-entries/{id}", wsId, created.id()))
                .andExpect(status().isNoContent());
    }

    @Test
    void findAll_shouldReturnEmptyList() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/schedule-entries", wsId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}