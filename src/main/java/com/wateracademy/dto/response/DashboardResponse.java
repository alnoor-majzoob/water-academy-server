package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.ScheduleStatus;
import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(
        long totalCourses,
        long scheduledCourses,
        long unscheduledCourses,
        long totalTrainers,
        long totalVenues,
        long conflicts,
        List<Long> coursesByMonth,
        CoursesByType coursesByType,
        List<TrainerUtilization> trainerUtilization,
        List<UpcomingSession> upcomingSessions) {

    public record CoursesByType(long inPerson, long online, long external) {
    }

    public record TrainerUtilization(Long trainerId, String trainerName, long scheduledDays, long maxDaysPerMonth) {
    }

    public record UpcomingSession(
            Long id,
            String courseName,
            String trainerName,
            LocalDate startDate,
            ScheduleStatus status,
            boolean hasConflict) {
    }
}
