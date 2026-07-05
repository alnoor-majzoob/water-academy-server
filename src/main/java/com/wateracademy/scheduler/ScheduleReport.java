package com.wateracademy.scheduler;

import com.wateracademy.scheduler.ga.Chromosome;
import com.wateracademy.scheduler.ga.Gene;
import com.wateracademy.scheduler.ga.UnschedulableCourse;
import com.wateracademy.scheduler.model.Calendar;
import com.wateracademy.scheduler.model.Course;
import com.wateracademy.scheduler.model.DeliveryType;
import com.wateracademy.scheduler.model.Trainer;
import com.wateracademy.scheduler.model.Venue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ScheduleReport {

    public static final class Entry {
        public final int courseId;
        public final String courseName;
        public final String specialization;
        public final int durationDays;
        public final int expectedTrainees;
        public final String preferredCity;
        public final String beneficiary;
        public final DeliveryType deliveryType;
        public final int priority;
        public final int trainerId;
        public final String trainerName;
        public final String trainerCity;
        public final LocalDate startDate;
        public final LocalDate endDate;
        public final Integer venueId;
        public final String venueName;
        public final String venueCity;
        public final int numberOfTrainees;
        public final String status;
        public final String conflictNotes;

        public Entry(int courseId, String courseName, String specialization, int durationDays,
                     int expectedTrainees, String preferredCity, String beneficiary,
                     DeliveryType deliveryType, int priority,
                     int trainerId, String trainerName, String trainerCity,
                     LocalDate startDate, LocalDate endDate,
                     Integer venueId, String venueName, String venueCity,
                     int numberOfTrainees, String status, String conflictNotes) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.specialization = specialization;
            this.durationDays = durationDays;
            this.expectedTrainees = expectedTrainees;
            this.preferredCity = preferredCity;
            this.beneficiary = beneficiary;
            this.deliveryType = deliveryType;
            this.priority = priority;
            this.trainerId = trainerId;
            this.trainerName = trainerName;
            this.trainerCity = trainerCity;
            this.startDate = startDate;
            this.endDate = endDate;
            this.venueId = venueId;
            this.venueName = venueName;
            this.venueCity = venueCity;
            this.numberOfTrainees = numberOfTrainees;
            this.status = status;
            this.conflictNotes = conflictNotes == null ? "" : conflictNotes;
        }
    }

    public final double bestFitness;
    public final long elapsedMs;
    public final int populationSize;
    public final int offspringCount;

    private final List<Entry> entries;
    private final List<UnschedulableCourse> unschedulable;

    public ScheduleReport(List<Entry> entries, List<UnschedulableCourse> unschedulable,
                          double bestFitness, long elapsedMs, int populationSize, int offspringCount) {
        this.entries = List.copyOf(entries);
        this.unschedulable = List.copyOf(unschedulable);
        this.bestFitness = bestFitness;
        this.elapsedMs = elapsedMs;
        this.populationSize = populationSize;
        this.offspringCount = offspringCount;
    }

    public List<Entry> getEntries() { return entries; }
    public List<UnschedulableCourse> getUnschedulable() { return unschedulable; }

    public static ScheduleReport fromChromosome(Chromosome chromosome,
                                                 List<Course> courses,
                                                 List<Trainer> trainers,
                                                 List<Venue> venues,
                                                 Calendar calendar,
                                                 List<UnschedulableCourse> unschedulable,
                                                 double bestFitness,
                                                 long elapsedMs,
                                                 int populationSize,
                                                 int offspringCount) {
        Map<Integer, Course> cmap = new LinkedHashMap<>();
        for (Course c : courses) cmap.put(c.getId(), c);
        Map<Integer, Trainer> tmap = new LinkedHashMap<>();
        for (Trainer t : trainers) tmap.put(t.getId(), t);
        Map<Integer, Venue> vmap = new LinkedHashMap<>();
        for (Venue v : venues) vmap.put(v.getId(), v);

        List<LocalDate> workingDays = new ArrayList<>(calendar.getWorkingDays());
        workingDays.sort(LocalDate::compareTo);

        Map<Integer, Set<LocalDate>> trainerDays = new LinkedHashMap<>();
        Map<Integer, Set<LocalDate>> venueDays = new LinkedHashMap<>();
        Map<Integer, Set<LocalDate>> trainerConflicts = new LinkedHashMap<>();
        Map<Integer, Set<LocalDate>> venueConflicts = new LinkedHashMap<>();
        for (Map.Entry<Integer, Gene> e : chromosome.orderedEntries()) {
            Gene g = e.getValue();
            Course c = cmap.get(e.getKey());
            if (c == null || g.getStartDate() == null) continue;
            List<LocalDate> occupied = occupiedWorkingDays(
                    g, c, workingDays, tmap, vmap);
            Set<LocalDate> td = trainerDays.computeIfAbsent(g.getTrainerId(), k -> new HashSet<>());
            for (LocalDate day : occupied) {
                if (!td.add(day)) {
                    trainerConflicts.computeIfAbsent(e.getKey(), k -> new HashSet<>()).add(day);
                }
            }
            if (g.getVenueId() != null) {
                Set<LocalDate> vd = venueDays.computeIfAbsent(g.getVenueId(), k -> new HashSet<>());
                for (LocalDate day : occupied) {
                    if (!vd.add(day)) {
                        venueConflicts.computeIfAbsent(e.getKey(), k -> new HashSet<>()).add(day);
                    }
                }
            }
        }

        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Integer, Gene> e : chromosome.orderedEntries()) {
            int courseId = e.getKey();
            Gene g = e.getValue();
            Course c = cmap.get(courseId);
            if (c == null) continue;
            Trainer t = tmap.get(c.getTrainerId());
            Venue v = g.getVenueId() == null ? null : vmap.get(g.getVenueId());

            List<LocalDate> occupied = occupiedWorkingDays(
                    g, c, workingDays, tmap, vmap);

            StringBuilder notes = new StringBuilder();
            Set<LocalDate> tc = trainerConflicts.get(courseId);
            if (tc != null && !tc.isEmpty()) {
                notes.append("Trainer conflict on ").append(tc);
            }
            Set<LocalDate> vc = venueConflicts.get(courseId);
            if (vc != null && !vc.isEmpty()) {
                if (notes.length() > 0) notes.append("; ");
                notes.append("Venue conflict on ").append(vc);
            }
            if (v != null && v.getCapacity() < c.getExpectedTrainees()) {
                if (notes.length() > 0) notes.append("; ");
                notes.append("Capacity ").append(v.getCapacity())
                        .append(" < trainees ").append(c.getExpectedTrainees());
            }

            LocalDate endDate = occupied.isEmpty() ? null : occupied.get(occupied.size() - 1);

            entries.add(new Entry(
                    c.getId(), c.getName(), c.getSpecialization(), c.getDurationDays(),
                    c.getExpectedTrainees(), c.getPreferredCity(), c.getBeneficiary(),
                    c.getDeliveryType(), c.getPriority(),
                    c.getTrainerId(),
                    t == null ? "(unknown)" : t.getName(),
                    t == null ? "" : t.getCity(),
                    g.getStartDate(), endDate,
                    v == null ? null : v.getId(),
                    v == null ? "" : v.getName(),
                    v == null ? "" : v.getCity(),
                    c.getExpectedTrainees(),
                    g.getStartDate() == null ? "Unscheduled" : "Scheduled",
                    notes.toString()));
        }
        return new ScheduleReport(entries, unschedulable, bestFitness, elapsedMs, populationSize, offspringCount);
    }

    private static List<LocalDate> occupiedWorkingDays(Gene g, Course c,
                                                        List<LocalDate> workingDays,
                                                        Map<Integer, Trainer> tmap,
                                                        Map<Integer, Venue> vmap) {
        if (g.getStartDate() == null) return List.of();
        int startIdx = workingDays.indexOf(g.getStartDate());
        if (startIdx < 0) return List.of();

        Set<LocalDate> blocked = new HashSet<>();
        Trainer t = tmap.get(g.getTrainerId());
        if (t != null) blocked.addAll(t.getUnavailableDates());
        if (g.getVenueId() != null) {
            Venue v = vmap.get(g.getVenueId());
            if (v != null) blocked.addAll(v.getUnavailableDates());
        }

        int span = c.getDurationDays();
        List<LocalDate> result = new ArrayList<>();
        for (int i = startIdx; i < workingDays.size() && result.size() < span; i++) {
            LocalDate d = workingDays.get(i);
            if (!blocked.contains(d)) {
                result.add(d);
            }
        }
        return result;
    }
}
