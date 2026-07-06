package com.wateracademy.scheduler.ga;

import com.wateracademy.scheduler.model.Calendar;
import com.wateracademy.scheduler.model.Course;
import com.wateracademy.scheduler.model.DeliveryType;
import com.wateracademy.scheduler.model.Trainer;
import com.wateracademy.scheduler.model.Venue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ScheduleFitnessEvaluator
        implements org.uncommons.watchmaker.framework.FitnessEvaluator<Chromosome> {

    private final Map<Integer, Course> courseById;
    private final Map<Integer, Trainer> trainerById;
    private final Map<Integer, Venue> venueById;
    private final Calendar calendar;
    private final int totalCourses;
    private final List<LocalDate> sortedWorkingDays;
    private final Map<LocalDate, Integer> workingDayIndex;

    private static final double BASE_PER_SCHEDULED = 100.0;
    private static final double PENALTY_UNSCHEDULED = 50.0;
    private static final double PENALTY_TRAINER_DAY_CONFLICT = 30.0;
    private static final double PENALTY_VENUE_DAY_CONFLICT = 30.0;
    private static final double PENALTY_HOLIDAY_START = 20.0;
    private static final double PENALTY_CAPACITY = 25.0;
    private static final double BONUS_CITY_MATCH = 5.0;
    private static final double BONUS_PRIORITY = 1.0;
    private static final double BONUS_EARLY_BUCKET = 15.0;
    private static final int BUCKET_SIZE = 15;

    public ScheduleFitnessEvaluator(List<Course> courses,
                                    List<Trainer> trainers,
                                    List<Venue> venues,
                                    Calendar calendar) {
        Map<Integer, Course> cmap = new HashMap<>();
        for (Course c : courses) cmap.put(c.getId(), c);
        Map<Integer, Trainer> tmap = new HashMap<>();
        for (Trainer t : trainers) tmap.put(t.getId(), t);
        Map<Integer, Venue> vmap = new HashMap<>();
        for (Venue v : venues) vmap.put(v.getId(), v);
        this.courseById = Map.copyOf(cmap);
        this.trainerById = Map.copyOf(tmap);
        this.venueById = Map.copyOf(vmap);
        this.calendar = calendar;
        this.totalCourses = courses.size();
        this.sortedWorkingDays = calendar.getWorkingDays().stream().sorted().toList();
        Map<LocalDate, Integer> idx = new HashMap<>();
        for (int i = 0; i < sortedWorkingDays.size(); i++) {
            idx.put(sortedWorkingDays.get(i), i);
        }
        this.workingDayIndex = Collections.unmodifiableMap(idx);
    }

    @Override
    public double getFitness(Chromosome candidate, List<? extends Chromosome> population) {
        if (candidate == null) return 0.0;
        double score = 0.0;

        Map<Integer, Set<LocalDate>> trainerDays = new HashMap<>();
        Map<Integer, Set<LocalDate>> venueDays = new HashMap<>();

        int unscheduled = 0;

        for (java.util.Map.Entry<Integer, Gene> e : candidate.orderedEntries()) {
            int courseId = e.getKey();
            Gene g = e.getValue();
            Course course = courseById.get(courseId);
            if (course == null) continue;

            if (g.getStartDate() == null) {
                unscheduled++;
                continue;
            }
            score += BASE_PER_SCHEDULED;
            int maxPriority = Math.max(1, totalCourses);
            int bonus = Math.max(0, maxPriority - course.getPriority() + 1);
            score += BONUS_PRIORITY * bonus;

            Integer startIdx = workingDayIndex.get(g.getStartDate());
            if (startIdx != null) {
                int bucket = startIdx / BUCKET_SIZE;
                int totalBuckets = (int) Math.ceil((double) sortedWorkingDays.size() / BUCKET_SIZE);
                score += BONUS_EARLY_BUCKET * Math.max(0, totalBuckets - bucket);
            }

            if (calendar.isHoliday(g.getStartDate())) {
                score -= PENALTY_HOLIDAY_START;
            }

           Set<LocalDate> unavailable = new HashSet<>();
            Trainer trainer = trainerById.get(g.getTrainerId());
            if (trainer != null) {
                unavailable.addAll(trainer.getUnavailableDates());
            }
            if (g.getVenueId() != null) {
                Venue venue = venueById.get(g.getVenueId());
                if (venue != null) {
                    unavailable.addAll(venue.getUnavailableDates());
                }
            }
            List<LocalDate> occupied = occupiedWorkingDays(g.getStartDate(), course.getDurationDays(), unavailable);
            if (occupied.isEmpty()) continue;

            Set<LocalDate> td = trainerDays.computeIfAbsent(g.getTrainerId(), k -> new HashSet<>());
            for (LocalDate d : occupied) {
                if (!td.add(d)) {
                    score -= PENALTY_TRAINER_DAY_CONFLICT;
                }
            }

            if (course.getDeliveryType() != DeliveryType.ONLINE) {
                if (g.getVenueId() == null) {
                    score -= PENALTY_VENUE_DAY_CONFLICT;
                } else {
                    Venue venue = venueById.get(g.getVenueId());
                    if (venue == null) {
                        score -= PENALTY_VENUE_DAY_CONFLICT;
                    } else {
                        if (venue.getCapacity() < course.getExpectedTrainees()) {
                            score -= PENALTY_CAPACITY;
                        }
                        if (venue.getCity().equals(course.getPreferredCity())) {
                            score += BONUS_CITY_MATCH;
                        }
                        Set<LocalDate> vd = venueDays.computeIfAbsent(venue.getId(), k -> new HashSet<>());
                        for (LocalDate d : occupied) {
                            if (!vd.add(d)) {
                                score -= PENALTY_VENUE_DAY_CONFLICT;
                            }
                        }
                    }
                }
            }
        }

        score -= PENALTY_UNSCHEDULED * unscheduled;
        return Math.max(0.0, score);
    }

    @Override
    public boolean isNatural() {
        return true;
    }

    private List<LocalDate> occupiedWorkingDays(LocalDate start, int durationDays, Set<LocalDate> unavailable) {
        Integer startIdx = workingDayIndex.get(start);
        if (startIdx == null) return List.of();
        List<LocalDate> result = new ArrayList<>();
        Set<LocalDate> blocked = Objects.requireNonNullElseGet(unavailable, Set::of);
        for (int i = startIdx; i < sortedWorkingDays.size() && result.size() < durationDays; i++) {
            if (!blocked.contains(sortedWorkingDays.get(i))) {
                result.add(sortedWorkingDays.get(i));
            }
        }
        return result;
    }
}
