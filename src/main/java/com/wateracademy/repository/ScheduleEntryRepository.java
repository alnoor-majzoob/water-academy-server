package com.wateracademy.repository;

import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {
    @EntityGraph(attributePaths = {"course", "trainer", "venue"})
    List<ScheduleEntry> findByWorkspaceId(Long workspaceId);

    @EntityGraph(attributePaths = {"course", "trainer", "venue"})
    @Query("""
            SELECT s FROM ScheduleEntry s
            WHERE s.workspace.id = :workspaceId
              AND (:status IS NULL OR s.status = :status)
              AND (:city IS NULL OR s.venue.city = :city)
              AND (:from IS NULL OR s.endDate >= :from)
              AND (:to IS NULL OR s.startDate <= :to)
              AND (:trainerId IS NULL OR s.trainer.id = :trainerId)
              AND (:venueId IS NULL OR s.venue.id = :venueId)
              AND (:courseId IS NULL OR s.course.id = :courseId)
              AND (:hasConflict IS NULL OR (:hasConflict = TRUE AND s.conflictNotes IS NOT NULL AND s.conflictNotes <> '') OR (:hasConflict = FALSE AND (s.conflictNotes IS NULL OR s.conflictNotes = '')))
            """)
    Page<ScheduleEntry> searchByWorkspaceId(@Param("workspaceId") Long workspaceId,
                                             @Param("status") ScheduleStatus status,
                                             @Param("city") String city,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to,
                                             @Param("trainerId") Long trainerId,
                                             @Param("venueId") Long venueId,
                                             @Param("courseId") Long courseId,
                                             @Param("hasConflict") Boolean hasConflict,
                                             Pageable pageable);

    @Query("SELECT s.course.id FROM ScheduleEntry s WHERE s.workspace.id = :workspaceId AND (s.status = 'CONFIRMED' OR s.status = 'COMPLETED')")
    Set<Long> findLockedCourseIdsByWorkspaceId(@Param("workspaceId") Long workspaceId);

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
