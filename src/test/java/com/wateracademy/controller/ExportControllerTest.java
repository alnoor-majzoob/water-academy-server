package com.wateracademy.controller;

import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private Long createWorkspace() throws Exception {
        var json = mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("WS", null, 2026, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class).id();
    }

    @Test
    void exportExcel_shouldReturnOctetStream() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/export", wsId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"water-academy-export.xlsx\""));
    }

    @Test
    void exportExcel_shouldIncludeData() throws Exception {
        var wsId = createWorkspace();

        mockMvc.perform(post("/api/workspaces/{wsId}/courses", wsId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CourseRequest("Export Course", null, 3, null, null,
                                        null, null, null, CourseType.IN_PERSON, null, null, null, null, null))));

        var result = mockMvc.perform(get("/api/workspaces/{wsId}/export", wsId))
                .andExpect(status().isOk())
                .andReturn();

        byte[] data = result.getResponse().getContentAsByteArray();
        assert data.length > 0 : "Export should produce non-empty output";
    }

    @Test
    void exportExcel_unknownWorkspace_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/workspaces/{wsId}/export", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void exportExcel_withSheetParam_returnsOnlyRequestedSheet() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/export", wsId)
                        .param("sheets", "courses"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"water-academy-export-courses.xlsx\""));
    }

    @Test
    void exportExcel_withTypeParam_schedule() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/export", wsId)
                        .param("type", "schedule"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"water-academy-export-schedule.xlsx\""));
    }

    @Test
    void exportExcel_withTypeParam_conflicts() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/export", wsId)
                        .param("type", "conflicts"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"water-academy-export-conflicts.xlsx\""));
    }

    @Test
    void exportExcel_withTypeParam_unscheduled() throws Exception {
        var wsId = createWorkspace();
        mockMvc.perform(get("/api/workspaces/{wsId}/export", wsId)
                        .param("type", "unscheduled"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"water-academy-export-unscheduled.xlsx\""));
    }
}