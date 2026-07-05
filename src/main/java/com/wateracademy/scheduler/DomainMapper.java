package com.wateracademy.scheduler;

import com.wateracademy.entity.CalendarDay;
import com.wateracademy.entity.CourseAssignment;
import com.wateracademy.entity.ScheduleEntry;
import com.wateracademy.entity.Trainer;
import com.wateracademy.entity.Venue;
import com.wateracademy.entity.enums.CourseType;
import com.wateracademy.entity.enums.ScheduleStatus;
import com.wateracademy.scheduler.model.Calendar;
import com.wateracademy.scheduler.model.CourseStatus;
import com.wateracademy.scheduler.model.DeliveryType;
import com.wateracademy.scheduler.model.VenueType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DomainMapper {

    private DomainMapper() {}

    public static List<com.wateracademy.scheduler.model.Course> toGaCourses(
            List<com.wateracademy.entity.Course> entities,
            List<CourseAssignment> assignments,
            List<ScheduleEntry> existingEntries) {

        Map<Long, Integer> courseToTrainer = new HashMap<>();
        for (CourseAssignment a : assignments) {
            courseToTrainer.put(a.getCourse().getId(), a.getTrainer().getId().intValue());
        }

        Set<Long> lockedCourseIds = new HashSet<>();
        for (ScheduleEntry e : existingEntries) {
            if (e.getStatus() == ScheduleStatus.CONFIRMED || e.getStatus() == ScheduleStatus.COMPLETED) {
                lockedCourseIds.add(e.getCourse().getId());
            }
        }

        Map<Long, ScheduleEntry> lockedEntries = new HashMap<>();
        for (ScheduleEntry e : existingEntries) {
            if (lockedCourseIds.contains(e.getCourse().getId())) {
                lockedEntries.putIfAbsent(e.getCourse().getId(), e);
            }
        }

        return entities.stream()
                .map(c -> toGaCourse(c, courseToTrainer, lockedEntries))
                .collect(Collectors.toList());
    }

    private static com.wateracademy.scheduler.model.Course toGaCourse(
            com.wateracademy.entity.Course entity,
            Map<Long, Integer> courseToTrainer,
            Map<Long, ScheduleEntry> lockedEntries) {

        int gaId = entity.getId().intValue();
        int trainerId = courseToTrainer.getOrDefault(entity.getId(), -1);
        DeliveryType deliveryType = mapDeliveryType(entity.getType());
        int priority = mapPriority(entity.getPriority());

        ScheduleEntry locked = lockedEntries.get(entity.getId());
        if (locked != null) {
            java.time.LocalDate lockedStart = locked.getStartDate();
            Integer lockedVenue = locked.getVenue() != null ? locked.getVenue().getId().intValue() : null;
            return new com.wateracademy.scheduler.model.Course(
                    gaId, entity.getName(), entity.getSpecialization(),
                    entity.getDurationDays(), entity.getExpectedTrainees(),
                    entity.getCity(), entity.getBeneficiary(),
                    priority, deliveryType, trainerId, CourseStatus.CONFIRMED,
                    lockedStart, lockedVenue);
        }

        return new com.wateracademy.scheduler.model.Course(
                gaId, entity.getName(), entity.getSpecialization(),
                entity.getDurationDays(), entity.getExpectedTrainees(),
                entity.getCity(), entity.getBeneficiary(),
                priority, deliveryType, trainerId, CourseStatus.PENDING);
    }

    public static List<com.wateracademy.scheduler.model.Trainer> toGaTrainers(List<Trainer> entities) {
        return entities.stream()
                .map(DomainMapper::toGaTrainer)
                .collect(Collectors.toList());
    }

    private static com.wateracademy.scheduler.model.Trainer toGaTrainer(Trainer entity) {
        Set<LocalDate> unavailable = parseDateList(entity.getUnavailableDates());
        return new com.wateracademy.scheduler.model.Trainer(
                entity.getId().intValue(),
                entity.getName(),
                entity.getCity() != null ? entity.getCity() : "",
                unavailable);
    }

    public static List<com.wateracademy.scheduler.model.Venue> toGaVenues(List<Venue> entities) {
        return entities.stream()
                .map(DomainMapper::toGaVenue)
                .collect(Collectors.toList());
    }

    private static com.wateracademy.scheduler.model.Venue toGaVenue(Venue entity) {
        Set<LocalDate> unavailable = parseDateList(entity.getUnavailableDates());
        return new com.wateracademy.scheduler.model.Venue(
                entity.getId().intValue(),
                entity.getName(),
                entity.getCity() != null ? entity.getCity() : "",
                entity.getCapacity() != null ? entity.getCapacity() : 1,
                mapVenueType(entity.getType()),
                unavailable);
    }

    public static Calendar toGaCalendar(List<CalendarDay> days) {
        Set<LocalDate> workingDays = new HashSet<>();
        Set<LocalDate> holidays = new HashSet<>();
        for (CalendarDay d : days) {
            if (Boolean.TRUE.equals(d.getIsWorkDay())) {
                workingDays.add(d.getDate());
            }
            if (Boolean.TRUE.equals(d.getIsHoliday())) {
                holidays.add(d.getDate());
            }
        }
        return new Calendar(workingDays, holidays);
    }

    public static List<ScheduleEntry> toScheduleEntries(
            ScheduleReport report,
            com.wateracademy.entity.Workspace workspace,
            List<com.wateracademy.entity.Course> courses,
            List<Trainer> trainers,
            List<Venue> venues) {

        Map<Integer, com.wateracademy.entity.Course> courseMap = new HashMap<>();
        for (var c : courses) courseMap.put(c.getId().intValue(), c);
        Map<Integer, Trainer> trainerMap = new HashMap<>();
        for (var t : trainers) trainerMap.put(t.getId().intValue(), t);
        Map<Integer, Venue> venueMap = new HashMap<>();
        for (var v : venues) venueMap.put(v.getId().intValue(), v);

        List<ScheduleEntry> entries = new java.util.ArrayList<>();
        for (ScheduleReport.Entry e : report.getEntries()) {
            if (e.startDate == null) continue;

            var course = courseMap.get(e.courseId);
            var trainer = trainerMap.get(e.trainerId);
            var venue = e.venueId != null ? venueMap.get(e.venueId) : null;

            if (course == null || trainer == null) continue;

            ScheduleEntry entry = new ScheduleEntry();
            entry.setWorkspace(workspace);
            entry.setCourse(course);
            entry.setTrainer(trainer);
            entry.setVenue(venue);
            entry.setStartDate(e.startDate);
            entry.setEndDate(e.endDate != null ? e.endDate : e.startDate);
            entry.setStatus(ScheduleStatus.SCHEDULED);
            entry.setConflictNotes(e.conflictNotes.isEmpty() ? null : e.conflictNotes);
            entries.add(entry);
        }
        return entries;
    }

    static int mapPriority(String priority) {
        if (priority == null) return 3;
        return switch (priority.toUpperCase()) {
            case "HIGH" -> 1;
            case "MEDIUM" -> 3;
            case "LOW" -> 5;
            default -> 3;
        };
    }

    static DeliveryType mapDeliveryType(CourseType type) {
        if (type == null) return DeliveryType.IN_PERSON;
        return switch (type) {
            case IN_PERSON -> DeliveryType.IN_PERSON;
            case ONLINE -> DeliveryType.ONLINE;
            case EXTERNAL -> DeliveryType.EXTERNAL;
        };
    }

    static VenueType mapVenueType(CourseType type) {
        if (type == null) return VenueType.HALL;
        return switch (type) {
            case IN_PERSON -> VenueType.HALL;
            case ONLINE -> VenueType.ONLINE;
            case EXTERNAL -> VenueType.EXTERNAL;
        };
    }

    static Set<LocalDate> parseDateList(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return Collections.emptySet();
        return Arrays.stream(dateStr.split("[;,\n]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try { return LocalDate.parse(s); }
                    catch (Exception e) { return null; }
                })
                .filter(d -> d != null)
                .collect(Collectors.toSet());
    }
}
