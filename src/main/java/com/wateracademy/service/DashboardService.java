package com.wateracademy.service;

import com.wateracademy.dto.response.DashboardResponse;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.repository.CourseRepository;
import com.wateracademy.repository.ScheduleEntryRepository;
import com.wateracademy.repository.TrainerRepository;
import com.wateracademy.repository.VenueRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CourseRepository courseRepository;
    private final TrainerRepository trainerRepository;
    private final VenueRepository venueRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;

    public DashboardService(CourseRepository courseRepository,
                            TrainerRepository trainerRepository,
                            VenueRepository venueRepository,
                            ScheduleEntryRepository scheduleEntryRepository) {
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
        this.venueRepository = venueRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
    }

    public DashboardResponse getDashboard(Long workspaceId) {
        var totalCourses = courseRepository.countByWorkspaceId(workspaceId);
        var scheduledCourses = scheduleEntryRepository.countDistinctScheduledCoursesByWorkspaceId(workspaceId);
        var unscheduledCourses = Math.max(totalCourses - scheduledCourses, 0);
        var coursesByType = new DashboardResponse.CoursesByType(
                courseRepository.countByWorkspaceIdAndType(workspaceId, CourseType.IN_PERSON),
                courseRepository.countByWorkspaceIdAndType(workspaceId, CourseType.ONLINE),
                courseRepository.countByWorkspaceIdAndType(workspaceId, CourseType.EXTERNAL));

        return new DashboardResponse(
                totalCourses,
                scheduledCourses,
                unscheduledCourses,
                trainerRepository.countByWorkspaceId(workspaceId),
                venueRepository.countByWorkspaceId(workspaceId),
                scheduleEntryRepository.countConflictsByWorkspaceId(workspaceId),
                coursesByMonth(workspaceId),
                coursesByType,
                scheduleEntryRepository.findTrainerUtilization(workspaceId),
                scheduleEntryRepository.findUpcomingDashboardSessions(
                        workspaceId, ScheduleStatus.COMPLETED, PageRequest.of(0, 4)));
    }

    private List<Long> coursesByMonth(Long workspaceId) {
        var counts = new ArrayList<>(Collections.nCopies(12, 0L));
        scheduleEntryRepository.countByStartMonth(workspaceId).forEach(row -> {
            var month = ((Number) row[0]).intValue();
            var count = ((Number) row[1]).longValue();
            if (month >= 1 && month <= 12) {
                counts.set(month - 1, count);
            }
        });
        return counts;
    }
}
