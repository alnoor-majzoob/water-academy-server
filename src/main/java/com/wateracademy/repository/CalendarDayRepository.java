package com.wateracademy.repository;

import com.wateracademy.entity.CalendarDay;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, Long>, JpaSpecificationExecutor<CalendarDay> {
    @EntityGraph(attributePaths = {"workspace"})
    List<CalendarDay> findByWorkspaceId(Long workspaceId);

    Optional<CalendarDay> findByWorkspaceIdAndDate(Long workspaceId, LocalDate date);
}
