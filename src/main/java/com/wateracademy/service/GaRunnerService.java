package com.wateracademy.service;

import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.entity.enums.WorkspaceStatus;
import com.wateracademy.repository.CalendarDayRepository;
import com.wateracademy.repository.CourseAssignmentRepository;
import com.wateracademy.repository.CourseRepository;
import com.wateracademy.repository.ScheduleEntryRepository;
import com.wateracademy.repository.TrainerRepository;
import com.wateracademy.repository.VenueRepository;
import com.wateracademy.repository.WorkspaceRepository;
import com.wateracademy.scheduler.DomainMapper;
import com.wateracademy.scheduler.ScheduleReport;
import com.wateracademy.scheduler.model.Calendar;
import com.wateracademy.scheduler.model.Course;
import com.wateracademy.scheduler.model.Trainer;
import com.wateracademy.scheduler.model.Venue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GaRunnerService {

    private final WorkspaceRepository workspaceRepository;
    private final CourseRepository courseRepository;
    private final TrainerRepository trainerRepository;
    private final VenueRepository venueRepository;
    private final CalendarDayRepository calendarDayRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final TaskService taskService;
    private final TransactionTemplate transactionTemplate;

    public GaRunnerService(WorkspaceRepository workspaceRepository,
                           CourseRepository courseRepository,
                           TrainerRepository trainerRepository,
                           VenueRepository venueRepository,
                           CalendarDayRepository calendarDayRepository,
                           CourseAssignmentRepository courseAssignmentRepository,
                           ScheduleEntryRepository scheduleEntryRepository,
                           TaskService taskService,
                           TransactionTemplate transactionTemplate) {
        this.workspaceRepository = workspaceRepository;
        this.courseRepository = courseRepository;
        this.trainerRepository = trainerRepository;
        this.venueRepository = venueRepository;
        this.calendarDayRepository = calendarDayRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.scheduleEntryRepository = scheduleEntryRepository;
        this.taskService = taskService;
        this.transactionTemplate = transactionTemplate;
    }

    @Async("gaTaskExecutor")
    public void runGaAsync(Long workspaceId, Long taskId, String mode) {
        try {
            taskService.start(taskId);

            var existingEntries = scheduleEntryRepository.findByWorkspaceId(workspaceId);

            boolean isUpdate = "update".equalsIgnoreCase(mode);
            Set<Long> lockedCourseIds;
            if (isUpdate) {
                lockedCourseIds = existingEntries.stream()
                        .filter(e -> e.getStatus() == ScheduleStatus.CONFIRMED
                                  || e.getStatus() == ScheduleStatus.COMPLETED)
                        .map(e -> e.getCourse().getId())
                        .collect(Collectors.toSet());
            } else {
                lockedCourseIds = Set.of();
            }

            var workspace = workspaceRepository.findById(workspaceId)
                    .orElseThrow(() -> new com.wateracademy.exception.ResourceNotFoundException("Workspace", workspaceId));

            var courses = courseRepository.findByWorkspaceId(workspaceId);
            var trainers = trainerRepository.findByWorkspaceId(workspaceId);
            var venues = venueRepository.findByWorkspaceId(workspaceId);
            var calendarDays = calendarDayRepository.findByWorkspaceId(workspaceId);
            var assignments = courseAssignmentRepository.findByWorkspaceId(workspaceId);

            List<Course> gaCourses = DomainMapper.toGaCourses(courses, assignments, existingEntries);
            List<Trainer> gaTrainers = DomainMapper.toGaTrainers(trainers);
            List<Venue> gaVenues = DomainMapper.toGaVenues(venues);
            Calendar gaCalendar = DomainMapper.toGaCalendar(calendarDays);

            var config = new com.wateracademy.scheduler.SchedulerService.Config();
            var scheduler = new com.wateracademy.scheduler.SchedulerService(
                    gaCourses, gaTrainers, gaVenues, gaCalendar, config);
            ScheduleReport report = scheduler.run();

            transactionTemplate.executeWithoutResult(status -> {
                if (isUpdate) {
                    scheduleEntryRepository.deleteScheduledByWorkspaceId(workspaceId);
                } else {
                    scheduleEntryRepository.deleteAllByWorkspaceId(workspaceId);
                }

                var freshCourses = courseRepository.findByWorkspaceId(workspaceId);
                var freshTrainers = trainerRepository.findByWorkspaceId(workspaceId);
                var freshVenues = venueRepository.findByWorkspaceId(workspaceId);

                var newEntries = DomainMapper.toScheduleEntries(
                        report, workspace, freshCourses, freshTrainers, freshVenues);

                if (isUpdate) {
                    newEntries.removeIf(e -> lockedCourseIds.contains(e.getCourse().getId()));
                }

                scheduleEntryRepository.saveAll(newEntries);

                workspace.setStatus(WorkspaceStatus.OPTIMIZED);
                workspaceRepository.save(workspace);
            });

            int totalScheduled = (int) report.getEntries().stream()
                    .filter(e -> e.startDate != null).count();
            int totalCourses = report.getEntries().size();
            int unscheduled = report.getUnschedulable().size();

            String log = String.format(
                    "GA completed: %d/%d courses scheduled, %d unschedulable. " +
                    "Fitness: %.1f. Elapsed: %dms (μ=%d, λ=%d)",
                    totalScheduled, totalCourses, unscheduled,
                    report.bestFitness, report.elapsedMs,
                    report.populationSize, report.offspringCount);

            if (!report.getUnschedulable().isEmpty()) {
                StringBuilder sb = new StringBuilder(log);
                sb.append("\n\nUnschedulable courses:");
                for (var u : report.getUnschedulable()) {
                    sb.append("\n  [").append(u.courseId).append("] ")
                      .append(u.courseName).append(" -> ").append(u.reason);
                }
                log = sb.toString();
            }

            taskService.complete(taskId, log);

        } catch (Exception e) {
            taskService.fail(taskId,
                    "GA failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
