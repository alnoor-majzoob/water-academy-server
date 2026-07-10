package com.wateracademy.repository;

import com.wateracademy.entity.CalendarDay;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<CalendarDay> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace"})
    @Query("""
            SELECT c FROM CalendarDay c
            WHERE c.workspace.id = :workspaceId
              AND (:from IS NULL OR c.date >= :from)
              AND (:to IS NULL OR c.date <= :to)
              AND (:isWorkDay IS NULL OR c.isWorkDay = :isWorkDay)
              AND (:isHoliday IS NULL OR c.isHoliday = :isHoliday)
            """)
    Page<CalendarDay> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                           @Param("from") LocalDate from,
                                           @Param("to") LocalDate to,
                                           @Param("isWorkDay") Boolean isWorkDay,
                                           @Param("isHoliday") Boolean isHoliday,
                                           Pageable pageable);

    Optional<CalendarDay> findByWorkspaceIdAndDate(Long workspaceId, LocalDate date);
}
