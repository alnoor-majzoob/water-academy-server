package com.wateracademy.controller;

import com.wateracademy.service.ExportService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping
    public ResponseEntity<byte[]> exportExcel(
            @PathVariable Long workspaceId,
            @RequestParam(required = false, value = "sheets") List<String> sheets,
            @RequestParam(required = false, value = "type") String type) {
        Set<String> sheetSet = sheets != null ? new HashSet<>(sheets) : null;
        byte[] data = exportService.exportToExcel(workspaceId, sheetSet, type);

        String filename = buildFilename(type, sheets);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(filename).build());
        return ResponseEntity.ok().headers(headers).body(data);
    }

    private static String buildFilename(String type, List<String> sheets) {
        if (type != null && !type.isBlank()) {
            return "water-academy-export-" + type + ".xlsx";
        }
        if (sheets != null && sheets.size() == 1) {
            return "water-academy-export-" + sheets.get(0) + ".xlsx";
        }
        return "water-academy-export.xlsx";
    }
}