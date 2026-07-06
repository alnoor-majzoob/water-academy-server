package com.wateracademy.scheduler.ga;

import java.time.LocalDate;
import java.util.Random;
import java.util.Set;

public final class DateShiftMutation extends BaseGeneMutation {

    private final int maxShift;

    public DateShiftMutation(FeasibilityProvider feasibility,
                             Set<Integer> lockedCourseIds,
                             double mutationRate,
                             int maxShift) {
        super(feasibility, lockedCourseIds, mutationRate);
        this.maxShift = maxShift;
    }

    @Override
    protected Chromosome mutate(Chromosome c, Random rng) {
        MapMutationState state = new MapMutationState();
        for (var entry : c.orderedEntries()) {
            int courseId = entry.getKey();
            Gene gene = entry.getValue();

            if (lockedCourseIds.contains(courseId)) {
                state.put(courseId, gene);
                continue;
            }

            if (rng.nextDouble() >= mutationRate) {
                state.put(courseId, gene);
                continue;
            }

            if (gene.getStartDate() == null) {
                state.put(courseId, gene);
                continue;
            }

            Feasibility f = feasibility.getFeasibility(courseId);
            if (f == null || f.validStartDates.isEmpty()) {
                state.put(courseId, gene);
                continue;
            }

            int currentIndex = f.validStartDates.indexOf(gene.getStartDate());
            if (currentIndex < 0) {
                state.put(courseId, gene);
                continue;
            }

            int low = Math.max(0, currentIndex - maxShift);
            int high = Math.min(f.validStartDates.size() - 1, currentIndex + maxShift);
            if (high <= low) {
                state.put(courseId, gene);
                continue;
            }

            LocalDate newDate = f.validStartDates.get(low + rng.nextInt(high - low + 1));
            Integer venueId = gene.getVenueId();
            state.put(courseId, gene.copyWith(newDate, venueId));
        }
        return state.build(c);
    }

    private static final class MapMutationState {
        private final java.util.Map<Integer, Gene> genes = new java.util.LinkedHashMap<>();

        void put(int courseId, Gene gene) { genes.put(courseId, gene); }

        Chromosome build(Chromosome original) {
            return new Chromosome(genes);
        }
    }
}
