package com.wateracademy.repository;

import com.wateracademy.entity.CalendarDay;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarDayRepository extends JpaRepository<CalendarDay, UUID> {
    List<CalendarDay> findByWorkspaceId(UUID workspaceId);
    Optional<CalendarDay> findByWorkspaceIdAndDate(UUID workspaceId, LocalDate date);
}
