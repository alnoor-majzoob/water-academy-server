package com.wateracademy.controller;

import com.wateracademy.dto.request.WorkspaceRequest;
import java.io.ByteArrayOutputStream;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private UUID createWorkspace() throws Exception {
        var json = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceRequest("WS", null, 2026, null))))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(json, com.wateracademy.dto.response.WorkspaceResponse.class).id();
    }

    @Test
    void importExcel_withValidFile_shouldReturnOk() throws Exception {
        var wsId = createWorkspace();
        var file = createValidExcelFile();

        mockMvc.perform(multipart("/api/workspaces/{wsId}/import", wsId)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coursesParsed").value(1))
                .andExpect(jsonPath("$.trainersParsed").value(1))
                .andExpect(jsonPath("$.venuesParsed").value(1));
    }

    @Test
    void importExcel_withMalformedFile_shouldReturn400() throws Exception {
        var wsId = createWorkspace();
        var file = new MockMultipartFile("file", "bad.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, "not an excel".getBytes());

        mockMvc.perform(multipart("/api/workspaces/{wsId}/import", wsId)
                        .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importExcel_unknownWorkspace_shouldReturn404() throws Exception {
        var file = createValidExcelFile();
        mockMvc.perform(multipart("/api/workspaces/{wsId}/import", UUID.randomUUID())
                        .file(file))
                .andExpect(status().isNotFound());
    }

    private MockMultipartFile createValidExcelFile() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            var cs = wb.createSheet("Courses");
            cs.createRow(0).createCell(0).setCellValue("Course ID");
            var cRow = cs.createRow(1);
            cRow.createCell(0).setCellValue("C-001");
            cRow.createCell(2).setCellValue("Test Course");
            cRow.createCell(4).setCellValue(3);
            cRow.createCell(9).setCellValue("In-person");

            var ts = wb.createSheet("Trainers");
            ts.createRow(0).createCell(0).setCellValue("Trainer ID");
            var tRow = ts.createRow(1);
            tRow.createCell(0).setCellValue("T-001");
            tRow.createCell(1).setCellValue("Test Trainer");

            var vs = wb.createSheet("Venues");
            vs.createRow(0).createCell(0).setCellValue("Venue ID");
            var vRow = vs.createRow(1);
            vRow.createCell(0).setCellValue("V-001");
            vRow.createCell(1).setCellValue("Test Venue");
            vRow.createCell(4).setCellValue(20);

            var cals = wb.createSheet("Calendar");
            cals.createRow(0).createCell(0).setCellValue("Date");
            var calRow = cals.createRow(1);
            calRow.createCell(0).setCellValue("2026-01-01");
            calRow.createCell(2).setCellValue("Yes");
            calRow.createCell(3).setCellValue("No");

            var as = wb.createSheet("assigned course");
            as.createRow(0).createCell(0).setCellValue("Assigned ID");
            var aRow = as.createRow(1);
            aRow.createCell(1).setCellValue("C-001");
            aRow.createCell(2).setCellValue("T-001");

            var bos = new ByteArrayOutputStream();
            wb.write(bos);
            return new MockMultipartFile("file", "test.xlsx",
                    MediaType.APPLICATION_OCTET_STREAM_VALUE, bos.toByteArray());
        }
    }
}