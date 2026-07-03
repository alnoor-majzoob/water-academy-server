package com.wateracademy.repository;

import com.wateracademy.entity.CalendarDay;
import com.wateracademy.entity.Workspace;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CalendarDayRepositoryTest {

    @Autowired
    private CalendarDayRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    @Test
    void shouldSaveCalendarDay() {
        var workspace = persistWorkspace();

        var day = new CalendarDay();
        day.setWorkspace(workspace);
        day.setDate(LocalDate.of(2026, 7, 1));
        day.setIsWorkDay(true);
        day.setIsHoliday(false);

        var saved = repository.save(day);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(saved.getIsWorkDay()).isTrue();
        assertThat(saved.getIsHoliday()).isFalse();
    }

    @Test
    void shouldFindByWorkspaceAndDate() {
        var workspace = persistWorkspace();

        var day = new CalendarDay();
        day.setWorkspace(workspace);
        day.setDate(LocalDate.of(2026, 7, 1));
        day.setIsWorkDay(true);
        day.setIsHoliday(false);
        em.persist(day);

        var found = repository.findByWorkspaceIdAndDate(workspace.getId(), LocalDate.of(2026, 7, 1));

        assertThat(found).isPresent();
        assertThat(found.get().getIsWorkDay()).isTrue();
    }

    @Test
    void shouldFindByWorkspaceId() {
        var workspace = persistWorkspace();

        var day = new CalendarDay();
        day.setWorkspace(workspace);
        day.setDate(LocalDate.of(2026, 7, 1));
        day.setIsWorkDay(true);
        day.setIsHoliday(false);
        em.persist(day);

        List<CalendarDay> results = repository.findByWorkspaceId(workspace.getId());

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldRejectDuplicateDatePerWorkspace() {
        var workspace = persistWorkspace();

        var day1 = new CalendarDay();
        day1.setWorkspace(workspace);
        day1.setDate(LocalDate.of(2026, 7, 1));
        day1.setIsWorkDay(true);
        day1.setIsHoliday(false);
        em.persist(day1);

        var day2 = new CalendarDay();
        day2.setWorkspace(workspace);
        day2.setDate(LocalDate.of(2026, 7, 1));
        day2.setIsWorkDay(false);
        day2.setIsHoliday(true);

        assertThatThrownBy(() -> repository.saveAndFlush(day2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldAllowSameDateInDifferentWorkspace() {
        var ws1 = persistWorkspace();
        var ws2 = new Workspace();
        ws2.setName("Second WS");
        ws2.setYear(2026);
        em.persist(ws2);

        var day1 = new CalendarDay();
        day1.setWorkspace(ws1);
        day1.setDate(LocalDate.of(2026, 7, 1));
        day1.setIsWorkDay(true);
        day1.setIsHoliday(false);
        em.persist(day1);

        var day2 = new CalendarDay();
        day2.setWorkspace(ws2);
        day2.setDate(LocalDate.of(2026, 7, 1));
        day2.setIsWorkDay(true);
        day2.setIsHoliday(false);

        var saved = repository.save(day2);
        assertThat(saved.getId()).isNotNull();
    }
}
