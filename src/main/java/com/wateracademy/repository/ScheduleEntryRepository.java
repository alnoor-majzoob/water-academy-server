package com.wateracademy.repository;

import com.wateracademy.dto.response.DashboardResponse;
import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long>, JpaSpecificationExecutor<ScheduleEntry> {
    @EntityGraph(attributePaths = {"course", "trainer", "venue"})
    List<ScheduleEntry> findByWorkspaceId(Long workspaceId);

    @Query("SELECT s.course.id FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND (s.status = 'CONFIRMED' OR s.status = 'COMPLETED')")
    Set<Long> findLockedCourseIdsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(DISTINCT s.course.id) FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND (s.conflictNotes IS NULL OR s.conflictNotes = '')")
    long countDistinctScheduledCoursesByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT COUNT(s) FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.conflictNotes IS NOT NULL AND s.conflictNotes <> ''")
    long countConflictsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT MONTH(s.startDate), COUNT(s) FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId GROUP BY MONTH(s.startDate)")
    List<Object[]> countByStartMonth(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT new com.wateracademy.dto.response.DashboardResponse$TrainerUtilization(
                t.id,
                t.name,
                COUNT(s.id),
                COALESCE(t.maxDaysPerMonth, COUNT(s.id), 1)
            )
            FROM Trainer t
            LEFT JOIN ScheduleEntry s ON s.trainer = t AND s.workspace.id = :workspaceId
            WHERE t.workspace.id = :workspaceId
            GROUP BY t.id, t.name, t.maxDaysPerMonth
            ORDER BY t.name
            """)
    List<DashboardResponse.TrainerUtilization> findTrainerUtilization(@Param("workspaceId") Long workspaceId);

    @Query("""
            SELECT new com.wateracademy.dto.response.DashboardResponse$UpcomingSession(
                s.id,
                s.course.name,
                s.trainer.name,
                s.startDate,
                s.status,
                CASE WHEN s.conflictNotes IS NOT NULL AND s.conflictNotes <> '' THEN true ELSE false END
            )
            FROM ScheduleEntry s
            WHERE s.workspace.id = :workspaceId AND s.status <> :completedStatus
            ORDER BY s.startDate ASC, s.id ASC
            """)
    List<DashboardResponse.UpcomingSession> findUpcomingDashboardSessions(
            @Param("workspaceId") Long workspaceId,
            @Param("completedStatus") ScheduleStatus completedStatus,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.status = 'SCHEDULED'")
    void deleteScheduledByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId")
    void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Query("SELECT s FROM ScheduleEntry s JOIN FETCH s.course JOIN FETCH s.trainer LEFT JOIN FETCH s.venue WHERE s.workspace.id = :workspaceId AND s.venue.id = :venueId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleEntry> findVenueConflicts(@Param("workspaceId") Long workspaceId,
                                            @Param("venueId") Long venueId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ScheduleEntry s JOIN FETCH s.course JOIN FETCH s.trainer LEFT JOIN FETCH s.venue WHERE s.workspace.id = :workspaceId AND s.trainer.id = :trainerId AND s.startDate <= :endDate AND s.endDate >= :startDate")
    List<ScheduleEntry> findTrainerConflicts(@Param("workspaceId") Long workspaceId,
                                               @Param("trainerId") Long trainerId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT s.venue.city FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND s.venue.city IS NOT NULL AND s.venue.city <> '' ORDER BY s.venue.city")
    List<String> findDistinctCities(@Param("workspaceId") Long workspaceId);

}
