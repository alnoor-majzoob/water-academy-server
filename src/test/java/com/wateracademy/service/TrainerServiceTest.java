package com.wateracademy.service;

import com.wateracademy.dto.request.TrainerRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TrainerServiceTest {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private WorkspaceService workspaceService;

    private Long createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    @Test
    void create_shouldPersistTrainer() {
        var wsId = createWorkspace("Trainers Test");
        var request = new TrainerRequest("Dr. Ahmed", "Khartoum", "Management, Leadership",
                "Internal", "2026-02-15; 2026-02-16", 12, 5, 0, "Experienced");
        var response = trainerService.create(wsId, request);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Dr. Ahmed");
        assertThat(response.specialties()).isEqualTo("Management, Leadership");
        assertThat(response.maxDaysPerMonth()).isEqualTo(12);
    }

    @Test
    void findById_shouldReturnTrainer() {
        var wsId = createWorkspace("Find Trainer");
        var created = trainerService.create(wsId, new TrainerRequest("Dr. Sara", null, null, null, null, null, null, null, null));
        var found = trainerService.findById(created.id());
        assertThat(found.name()).isEqualTo("Dr. Sara");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> trainerService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllByWorkspaceId_shouldReturnScopedResults() {
        var ws1 = createWorkspace("WS1");
        var ws2 = createWorkspace("WS2");
        trainerService.create(ws1, new TrainerRequest("Ali", null, null, null, null, null, null, null, null));
        trainerService.create(ws1, new TrainerRequest("Sara", null, null, null, null, null, null, null, null));
        trainerService.create(ws2, new TrainerRequest("Omar", null, null, null, null, null, null, null, null));

        assertThat(trainerService.findAllByWorkspaceId(ws1)).hasSize(2);
        assertThat(trainerService.findAllByWorkspaceId(ws2)).hasSize(1);
    }

    @Test
    void update_shouldModifyFields() {
        var wsId = createWorkspace("Update Trainer");
        var created = trainerService.create(wsId, new TrainerRequest("Old", null, null, null, null, null, null, null, null));
        var updated = trainerService.update(created.id(), new TrainerRequest("New Name", "Jeddah", "Leadership",
                "External", "2026-07-01", 15, 4, 500, "Senior"));
        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.city()).isEqualTo("Jeddah");
        assertThat(updated.costPerDay()).isEqualTo(500);
        assertThat(updated.trainerType()).isEqualTo("External");
    }

    @Test
    void delete_shouldRemoveTrainer() {
        var wsId = createWorkspace("Delete Trainer");
        var created = trainerService.create(wsId, new TrainerRequest("To Delete", null, null, null, null, null, null, null, null));
        trainerService.delete(created.id());
        assertThatThrownBy(() -> trainerService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}