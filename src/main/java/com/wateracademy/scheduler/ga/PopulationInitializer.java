package com.wateracademy.scheduler.ga;

import com.wateracademy.scheduler.model.Calendar;
import com.wateracademy.scheduler.model.Course;
import com.wateracademy.scheduler.model.DeliveryType;
import com.wateracademy.scheduler.model.Trainer;
import com.wateracademy.scheduler.model.Venue;
import com.wateracademy.scheduler.model.VenueType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class PopulationInitializer {

    private final List<Course> prioritySortedCourses;
    // private final Map<Integer, Course> courseById;
    private final Map<Integer, Trainer> trainerById;
    private final Map<Integer, Venue> venueById;
    private final List<LocalDate> workingDays;
    private final Map<LocalDate, Integer> workingDayIndex;
    private final Map<Integer, Feasibility> feasibilityByCourse = new HashMap<>();
    private final List<UnschedulableCourse> unschedulable = new ArrayList<>();
    private final int populationSize;
    private final double greedyRatio;
    private final double clusteredRatio;
    private final Random random;

    public PopulationInitializer(List<Course> courses,
                                 List<Trainer> trainers,
                                 List<Venue> venues,
                                 Calendar calendar,
                                 int populationSize,
                                 double greedyRatio,
                                 double clusteredRatio,
                                 Long seed) {
        this.populationSize = populationSize;
        this.greedyRatio = greedyRatio;
        this.clusteredRatio = clusteredRatio;
        this.random = seed == null ? new Random() : new Random(seed);

        List<Course> sorted = new ArrayList<>(courses);
        sorted.sort((a, b) -> {
            int byPriority = Integer.compare(a.getPriority(), b.getPriority());
            return byPriority != 0 ? byPriority : Integer.compare(a.getId(), b.getId());
        });
        this.prioritySortedCourses = Collections.unmodifiableList(sorted);

        Map<Integer, Trainer> tmap = new HashMap<>();
        for (Trainer t : trainers) tmap.put(t.getId(), t);
        this.trainerById = Map.copyOf(tmap);
        Map<Integer, Venue> vmap = new HashMap<>();
        for (Venue v : venues) vmap.put(v.getId(), v);
        this.venueById = Map.copyOf(vmap);

        List<LocalDate> wd = new ArrayList<>(calendar.getWorkingDays());
        Collections.sort(wd);
        this.workingDays = Collections.unmodifiableList(wd);
        Map<LocalDate, Integer> idx = new HashMap<>();
        for (int i = 0; i < wd.size(); i++) idx.put(wd.get(i), i);
        this.workingDayIndex = Collections.unmodifiableMap(idx);

        precomputeFeasibility();
    }

    private void precomputeFeasibility() {
        for (Course course : prioritySortedCourses) {
            if (course.isLocked()) {
                Gene pinned = lockedGeneFor(course);
                if (pinned.getStartDate() == null) {
                    flag(course, "Course is CONFIRMED/EXECUTED but has no startDate to lock to.");
                }
                feasibilityByCourse.put(course.getId(),
                        new Feasibility(course.getId(), List.of(), List.of()));
                continue;
            }
            Trainer trainer = trainerById.get(course.getTrainerId());
            if (trainer == null) {
                flag(course, "Trainer id=" + course.getTrainerId() + " not found.");
                feasibilityByCourse.put(course.getId(),
                        new Feasibility(course.getId(), List.of(), List.of()));
                continue;
            }
            List<LocalDate> validStarts = computeValidStartDates(course, trainer);
            List<Venue> compatibleVenues =
                    course.getDeliveryType() == DeliveryType.ONLINE
                            ? List.of()
                            : computeCompatibleVenues(course);
            feasibilityByCourse.put(course.getId(),
                    new Feasibility(course.getId(), validStarts, compatibleVenues));
            if (validStarts.isEmpty()) {
                flag(course,
                        "Trainer \"" + trainer.getName() + "\" has no free "
                                + course.getDurationDays() + "-day working window within the year.");
            }
            if (course.getDeliveryType() != DeliveryType.ONLINE && compatibleVenues.isEmpty()) {
                flag(course,
                        "No venue in \"" + course.getPreferredCity()
                                + "\" with capacity >= " + course.getExpectedTrainees() + ".");
            }
        }
    }

    private void flag(Course course, String reason) {
        unschedulable.add(new UnschedulableCourse(
                course.getId(), course.getName(), reason));
    }

    private List<LocalDate> computeValidStartDates(Course course, Trainer trainer) {
        List<LocalDate> result = new ArrayList<>();
        Set<LocalDate> unavailable = trainer.getUnavailableDates();
        for (int i = 0; i < workingDays.size(); i++) {
            List<LocalDate> window = workingDaysFromIndex(i, course.getDurationDays());
            if (window.size() < course.getDurationDays()) break;
            boolean anyUnavailable = false;
            for (LocalDate d : window) {
                if (unavailable.contains(d) || !workingDayIndex.containsKey(d)) {
                    anyUnavailable = true;
                    break;
                }
            }
            if (!anyUnavailable) result.add(workingDays.get(i));
        }
        return result;
    }

    private List<LocalDate> workingDaysFromIndex(int startIndex, int count) {
        int end = Math.min(startIndex + count, workingDays.size());
        return workingDays.subList(startIndex, end);
    }

    private List<Venue> computeCompatibleVenues(Course course) {
        List<Venue> result = new ArrayList<>();
        for (Venue v : venueById.values()) {
            if (v.getType() == VenueType.ONLINE) continue;
            if (v.getCapacity() < course.getExpectedTrainees()) continue;
            if (!v.getCity().equals(course.getPreferredCity())) continue;
            result.add(v);
        }

        if( result.isEmpty()) {
            for (Venue v : venueById.values()) {
                if (v.getType() == VenueType.ONLINE) continue;
                if (v.getCapacity() < course.getExpectedTrainees()) continue;
                result.add(v);
            }
        }
        return result;
    }

    public List<UnschedulableCourse> getUnschedulable() {
        return Collections.unmodifiableList(unschedulable);
    }

    public Map<Integer, Feasibility> getFeasibility() {
        return Collections.unmodifiableMap(feasibilityByCourse);
    }

    public Set<Integer> lockedCourseIds() {
        Set<Integer> ids = new HashSet<>();
        for (Course c : prioritySortedCourses) {
            if (c.isLocked()) ids.add(c.getId());
        }
        return Collections.unmodifiableSet(ids);
    }

    public Gene lockedGeneFor(Course course) {
        LocalDate start = course.getLockedStartDate();
        Integer venue = course.getLockedVenueId();
        if (course.getDeliveryType() == DeliveryType.ONLINE) venue = null;
        return new Gene(course.getId(), course.getTrainerId(), start, venue);
    }

    public List<LocalDate> getWorkingDays() { return workingDays; }

    public Map<LocalDate, Integer> getWorkingDayIndex() { return workingDayIndex; }

    public List<Course> getPrioritySortedCourses() { return prioritySortedCourses; }

    public Map<Integer, Trainer> getTrainerById() { return trainerById; }

    public Map<Integer, Venue> getVenueById() { return venueById; }

    public Random getRandom() { return random; }

    public List<Chromosome> buildSeedPopulation() {
        List<Chromosome> population = new ArrayList<>();
        Set<String> signatures = new HashSet<>();

        int greedyCount = (int) Math.round(populationSize * greedyRatio);
        int clusteredCount = (int) Math.round(populationSize * clusteredRatio);

        for (int i = 0; i < greedyCount; i++) {
            tryAdd(population, signatures, buildGreedyChromosome());
        }
        for (int i = 0; i < clusteredCount; i++) {
            tryAdd(population, signatures, buildCityClusteredChromosome());
        }
        int attempts = 0;
        int maxAttempts = populationSize * 25;
        while (population.size() < populationSize && attempts < maxAttempts) {
            tryAdd(population, signatures, buildGuidedRandomChromosome());
            attempts++;
        }
        while (population.size() < populationSize) {
            population.add(buildGuidedRandomChromosome());
        }
        return population;
    }

    private void tryAdd(List<Chromosome> population, Set<String> signatures, Chromosome c) {
        if (signatures.add(c.signature())) population.add(c);
    }

    private Chromosome buildGreedyChromosome() {
        OccupancyTracker occ = new OccupancyTracker();
        Map<Integer, Gene> genes = new HashMap<>();
        for (Course course : prioritySortedCourses) {
            if (course.isLocked()) {
                Gene g = lockedGeneFor(course);
                genes.put(course.getId(), g);
                reserveLocked(occ, course, g);
                continue;
            }
            Gene g = assignGene(course, occ, false);
            genes.put(course.getId(), g);
        }
        return new Chromosome(genes);
    }

    private Chromosome buildCityClusteredChromosome() {
        Map<String, List<Course>> byCity = new HashMap<>();
        for (Course c : prioritySortedCourses) {
            byCity.computeIfAbsent(c.getPreferredCity(), k -> new ArrayList<>()).add(c);
        }
        List<String> cities = new ArrayList<>(byCity.keySet());
        Collections.shuffle(cities, random);

        OccupancyTracker occ = new OccupancyTracker();
        Map<Integer, Gene> genes = new HashMap<>();
        for (Course c : prioritySortedCourses) {
            if (c.isLocked()) {
                Gene g = lockedGeneFor(c);
                genes.put(c.getId(), g);
                reserveLocked(occ, c, g);
            }
        }
        for (String city : cities) {
            for (Course c : byCity.get(city)) {
                if (c.isLocked()) continue;
                Gene g = assignGene(c, occ, false);
                genes.put(c.getId(), g);
            }
        }
        return new Chromosome(genes);
    }

    private Chromosome buildGuidedRandomChromosome() {
        OccupancyTracker occ = new OccupancyTracker();
        Map<Integer, Gene> genes = new HashMap<>();
        for (Course course : prioritySortedCourses) {
            if (course.isLocked()) {
                Gene g = lockedGeneFor(course);
                genes.put(course.getId(), g);
                reserveLocked(occ, course, g);
                continue;
            }
            Gene g = assignGene(course, occ, true);
            genes.put(course.getId(), g);
        }
        return new Chromosome(genes);
    }

    public Gene randomGeneForCourse(Course course, Random rng) {
        OccupancyTracker occ = new OccupancyTracker();
        for (Course c : prioritySortedCourses) {
            if (c.isLocked() && c.getId() != course.getId()) {
                reserveLocked(occ, c, lockedGeneFor(c));
            }
        }
        return assignGene(course, occ, true, rng);
    }

    private void reserveLocked(OccupancyTracker occ, Course course, Gene g) {
        if (g.getStartDate() == null) return;
        List<LocalDate> occupied = workingDaysFromIndex(
                workingDayIndex.get(g.getStartDate()), course.getDurationDays());
        if (occupied.size() < course.getDurationDays()) return;
        occ.reserveTrainer(course.getTrainerId(), occupied);
        if (g.getVenueId() != null) {
            occ.reserveVenue(g.getVenueId(), occupied);
        }
    }

    private Gene assignGene(Course course, OccupancyTracker occ, boolean randomized) {
        return assignGene(course, occ, randomized, this.random);
    }

    private Gene assignGene(Course course, OccupancyTracker occ, boolean randomized, Random rng) {
        Feasibility f = feasibilityByCourse.get(course.getId());
        if (f == null || f.validStartDates.isEmpty()) {
            return new Gene(course.getId(), course.getTrainerId(), null, null);
        }
        List<LocalDate> candidateDates = new ArrayList<>(f.validStartDates);
        if (randomized) Collections.shuffle(candidateDates, rng);

        int maxDateAttempts = randomized
                ? Math.min(15, candidateDates.size())
                : candidateDates.size();

        for (int i = 0; i < maxDateAttempts; i++) {
            LocalDate start = candidateDates.get(i);
            List<LocalDate> occupied = workingDaysFromIndex(
                    workingDayIndex.get(start), course.getDurationDays());
            if (!occ.isTrainerFree(course.getTrainerId(), occupied)) continue;

            if (course.getDeliveryType() == DeliveryType.ONLINE) {
                occ.reserveTrainer(course.getTrainerId(), occupied);
                return new Gene(course.getId(), course.getTrainerId(), start, null);
            }
            List<Venue> venues = new ArrayList<>(f.compatibleVenues);
            if (randomized) Collections.shuffle(venues, rng);
            for (Venue v : venues) {
                if (occ.isVenueFree(v.getId(), occupied)) {
                    occ.reserveTrainer(course.getTrainerId(), occupied);
                    occ.reserveVenue(v.getId(), occupied);
                    return new Gene(course.getId(), course.getTrainerId(), start, v.getId());
                }
            }
        }
        LocalDate fallbackDate = f.validStartDates.get(0);
        Integer fallbackVenue = null;
        if (course.getDeliveryType() != DeliveryType.ONLINE && !f.compatibleVenues.isEmpty()) {
            fallbackVenue = f.compatibleVenues.get(rng.nextInt(f.compatibleVenues.size())).getId();
        }
        return new Gene(course.getId(), course.getTrainerId(), fallbackDate, fallbackVenue);
    }
}
