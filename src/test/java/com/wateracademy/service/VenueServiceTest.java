package com.wateracademy.service;

import com.wateracademy.dto.request.VenueRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class VenueServiceTest {

    @Autowired
    private VenueService venueService;

    @Autowired
    private WorkspaceService workspaceService;

    private UUID createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    @Test
    void create_shouldPersistVenue() {
        var wsId = createWorkspace("Venues Test");
        var request = new VenueRequest("Main Hall", "Khartoum", 50, CourseType.IN_PERSON,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                null, "Projector, Whiteboard");
        var response = venueService.create(wsId, request);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Main Hall");
        assertThat(response.capacity()).isEqualTo(50);
        assertThat(response.type()).isEqualTo(CourseType.IN_PERSON);
    }

    @Test
    void findById_shouldReturnVenue() {
        var wsId = createWorkspace("Find Venue");
        var created = venueService.create(wsId, new VenueRequest("Room A", null, 30, CourseType.IN_PERSON, null, null, null, null));
        var found = venueService.findById(created.id());
        assertThat(found.name()).isEqualTo("Room A");
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> venueService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllByWorkspaceId_shouldReturnScopedResults() {
        var ws1 = createWorkspace("WS1");
        var ws2 = createWorkspace("WS2");
        venueService.create(ws1, new VenueRequest("Hall A", null, 20, CourseType.IN_PERSON, null, null, null, null));
        venueService.create(ws1, new VenueRequest("Hall B", null, 30, CourseType.IN_PERSON, null, null, null, null));
        venueService.create(ws2, new VenueRequest("Online Room", null, 999, CourseType.ONLINE, null, null, null, null));

        assertThat(venueService.findAllByWorkspaceId(ws1)).hasSize(2);
        assertThat(venueService.findAllByWorkspaceId(ws2)).hasSize(1);
    }

    @Test
    void update_shouldModifyFields() {
        var wsId = createWorkspace("Update Venue");
        var created = venueService.create(wsId, new VenueRequest("Old", null, 10, CourseType.IN_PERSON, null, null, null, null));
        var updated = venueService.update(created.id(), new VenueRequest("New Hall", "Riyadh", 100, CourseType.ONLINE,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 31),
                "2026-08-01", "Full AV setup"));
        assertThat(updated.name()).isEqualTo("New Hall");
        assertThat(updated.city()).isEqualTo("Riyadh");
        assertThat(updated.capacity()).isEqualTo(100);
        assertThat(updated.type()).isEqualTo(CourseType.ONLINE);
    }

    @Test
    void delete_shouldRemoveVenue() {
        var wsId = createWorkspace("Delete Venue");
        var created = venueService.create(wsId, new VenueRequest("To Delete", null, 15, CourseType.IN_PERSON, null, null, null, null));
        venueService.delete(created.id());
        assertThatThrownBy(() -> venueService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}