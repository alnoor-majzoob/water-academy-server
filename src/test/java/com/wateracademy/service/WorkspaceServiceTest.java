package com.wateracademy.service;

import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.dto.request.WorkspaceStatusRequest;
import com.wateracademy.dto.response.WorkspaceResponse;
import com.wateracademy.entity.enums.WorkspaceStatus;
import com.wateracademy.exception.InvalidStatusTransitionException;
import com.wateracademy.exception.ResourceNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class WorkspaceServiceTest {

    @Autowired
    private WorkspaceService service;

    @Test
    void create_shouldPersistWorkspace() {
        var request = new WorkspaceRequest("Test WS", "A test workspace", 2026, "#FF5733");
        var response = service.create(request);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Test WS");
        assertThat(response.description()).isEqualTo("A test workspace");
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.color()).isEqualTo("#FF5733");
        assertThat(response.status()).isEqualTo(WorkspaceStatus.DRAFT);
    }

    @Test
    void findById_shouldReturnWorkspace() {
        var created = service.create(new WorkspaceRequest("Find Me", null, 2026, null));
        var found = service.findById(created.id());
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.name()).isEqualTo("Find Me");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnAllWorkspaces() {
        service.create(new WorkspaceRequest("WS1", null, 2026, null));
        service.create(new WorkspaceRequest("WS2", null, 2027, null));
        List<WorkspaceResponse> all = service.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void update_shouldModifyFields() {
        var created = service.create(new WorkspaceRequest("Original", "Desc", 2026, "#000"));
        var updated = service.update(created.id(), new WorkspaceRequest("Updated", "New desc", 2027, "#FFF"));
        assertThat(updated.name()).isEqualTo("Updated");
        assertThat(updated.description()).isEqualTo("New desc");
        assertThat(updated.year()).isEqualTo(2027);
        assertThat(updated.color()).isEqualTo("#FFF");
    }

    @Test
    void updateStatus_shouldTransitionFromDraftToImported() {
        var created = service.create(new WorkspaceRequest("WS", null, 2026, null));
        var result = service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.IMPORTED));
        assertThat(result.status()).isEqualTo(WorkspaceStatus.IMPORTED);
    }

    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        var created = service.create(new WorkspaceRequest("WS", null, 2026, null));
        service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.IMPORTED));
        assertThatThrownBy(() ->
                service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.DISABLED)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void updateStatus_shouldRejectDraftToOptimized() {
        var created = service.create(new WorkspaceRequest("WS", null, 2026, null));
        assertThatThrownBy(() ->
                service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.OPTIMIZED)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void updateStatus_shouldTransitionFromDraftToDisabled() {
        var created = service.create(new WorkspaceRequest("WS", null, 2026, null));
        var result = service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.DISABLED));
        assertThat(result.status()).isEqualTo(WorkspaceStatus.DISABLED);
    }

    @Test
    void updateStatus_shouldTransitionFromOptimizedToImported() {
        var created = service.create(new WorkspaceRequest("WS", null, 2026, null));
        service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.IMPORTED));
        service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.OPTIMIZED));
        var result = service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.IMPORTED));
        assertThat(result.status()).isEqualTo(WorkspaceStatus.IMPORTED);
    }

    @Test
    void updateStatus_shouldRejectDisabledToAnything() {
        var created = service.create(new WorkspaceRequest("WS", null, 2026, null));
        service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.DISABLED));
        assertThatThrownBy(() ->
                service.updateStatus(created.id(), new WorkspaceStatusRequest(WorkspaceStatus.DRAFT)))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldRemoveWorkspace() {
        var created = service.create(new WorkspaceRequest("To Delete", null, 2026, null));
        service.delete(created.id());
        assertThatThrownBy(() -> service.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}