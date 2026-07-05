package com.wateracademy.service;

import com.wateracademy.dto.request.CalendarDayRequest;
import com.wateracademy.dto.request.WorkspaceRequest;
import com.wateracademy.exception.DuplicateResourceException;
import com.wateracademy.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CalendarDayServiceTest {

    @Autowired
    private CalendarDayService calendarDayService;

    @Autowired
    private WorkspaceService workspaceService;

    private Long createWorkspace(String name) {
        return workspaceService.create(new WorkspaceRequest(name, null, 2026, null)).id();
    }

    @Test
    void create_shouldPersistDay() {
        var wsId = createWorkspace("Calendar Test");
        var response = calendarDayService.create(wsId,
                new CalendarDayRequest(LocalDate.of(2026, 7, 1), true, false));
        assertThat(response.id()).isNotNull();
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(response.isWorkDay()).isTrue();
        assertThat(response.isHoliday()).isFalse();
    }

    @Test
    void create_shouldRejectDuplicateDate() {
        var wsId = createWorkspace("Dup Test");
        calendarDayService.create(wsId, new CalendarDayRequest(LocalDate.of(2026, 7, 1), true, false));
        assertThatThrownBy(() ->
                calendarDayService.create(wsId, new CalendarDayRequest(LocalDate.of(2026, 7, 1), false, true)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void bulkCreate_shouldInsertMultipleDays() {
        var wsId = createWorkspace("Bulk Test");
        var requests = List.of(
                new CalendarDayRequest(LocalDate.of(2026, 1, 1), true, false),
                new CalendarDayRequest(LocalDate.of(2026, 1, 2), true, false),
                new CalendarDayRequest(LocalDate.of(2026, 1, 3), false, true));
        var results = calendarDayService.bulkCreate(wsId, requests);
        assertThat(results).hasSize(3);
        assertThat(calendarDayService.findAllByWorkspaceId(wsId)).hasSize(3);
    }

    @Test
    void findAllByWorkspaceId_shouldReturnScopedResults() {
        var ws1 = createWorkspace("WS1");
        var ws2 = createWorkspace("WS2");
        calendarDayService.create(ws1, new CalendarDayRequest(LocalDate.of(2026, 1, 1), true, false));
        calendarDayService.create(ws1, new CalendarDayRequest(LocalDate.of(2026, 1, 2), true, false));
        calendarDayService.create(ws2, new CalendarDayRequest(LocalDate.of(2026, 1, 1), false, true));

        assertThat(calendarDayService.findAllByWorkspaceId(ws1)).hasSize(2);
        assertThat(calendarDayService.findAllByWorkspaceId(ws2)).hasSize(1);
    }

    @Test
    void findById_shouldReturnDay() {
        var wsId = createWorkspace("Find Day");
        var created = calendarDayService.create(wsId, new CalendarDayRequest(LocalDate.of(2026, 6, 15), true, false));
        var found = calendarDayService.findById(created.id());
        assertThat(found.date()).isEqualTo(LocalDate.of(2026, 6, 15));
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThatThrownBy(() -> calendarDayService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldModifyFields() {
        var wsId = createWorkspace("Update Day");
        var created = calendarDayService.create(wsId, new CalendarDayRequest(LocalDate.of(2026, 7, 1), true, false));
        var updated = calendarDayService.update(created.id(),
                new CalendarDayRequest(LocalDate.of(2026, 7, 4), false, true));
        assertThat(updated.date()).isEqualTo(LocalDate.of(2026, 7, 4));
        assertThat(updated.isWorkDay()).isFalse();
        assertThat(updated.isHoliday()).isTrue();
    }

    @Test
    void delete_shouldRemoveDay() {
        var wsId = createWorkspace("Delete Day");
        var created = calendarDayService.create(wsId, new CalendarDayRequest(LocalDate.of(2026, 12, 25), false, true));
        calendarDayService.delete(created.id());
        assertThatThrownBy(() -> calendarDayService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}