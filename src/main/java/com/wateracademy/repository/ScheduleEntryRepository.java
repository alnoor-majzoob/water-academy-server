package com.wateracademy.repository;

import com.wateracademy.entity.ScheduleEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
    List<ScheduleEntry> findByWorkspaceId(Long workspaceId);


    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.status = 'SCHEDULED'")
    void deleteScheduledByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT s FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.venue.id = :venueId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleEntry> findVenueConflicts(@Param("workspaceId") Long workspaceId,
                                            @Param("venueId") Long venueId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.trainer.id = :trainerId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleEntry> findTrainerConflicts(@Param("workspaceId") Long workspaceId,
                                              @Param("trainerId") Long trainerId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);
}
