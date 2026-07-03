package com.wateracademy.repository;

import com.wateracademy.entity.ScheduleEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, UUID> {
    List<ScheduleEntry> findByWorkspaceId(UUID workspaceId);

    @Query("SELECT s FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.venue.id = :venueId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleEntry> findVenueConflicts(@Param("workspaceId") UUID workspaceId,
                                            @Param("venueId") UUID venueId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.trainer.id = :trainerId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleEntry> findTrainerConflicts(@Param("workspaceId") UUID workspaceId,
                                              @Param("trainerId") UUID trainerId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}
