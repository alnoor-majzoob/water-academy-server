package com.wateracademy.controller;

import com.wateracademy.dto.response.ImportResult;
import com.wateracademy.service.ImportService;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<ImportResult> importExcel(@PathVariable UUID workspaceId,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        var result = importService.importExcel(file.getInputStream(), workspaceId);
        return ResponseEntity.ok(result);
    }
}