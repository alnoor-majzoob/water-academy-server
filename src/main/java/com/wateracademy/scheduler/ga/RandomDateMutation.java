package com.wateracademy.scheduler.ga;

import java.time.LocalDate;
import java.util.Random;
import java.util.Set;

public final class RandomDateMutation extends BaseGeneMutation {

    public RandomDateMutation(FeasibilityProvider feasibility,
                              Set<Integer> lockedCourseIds,
                              double mutationRate) {
        super(feasibility, lockedCourseIds, mutationRate);
    }

    @Override
    protected Chromosome mutate(Chromosome c, Random rng) {
        java.util.Map<Integer, Gene> genes = new java.util.LinkedHashMap<>();
        for (var entry : c.orderedEntries()) {
            int courseId = entry.getKey();
            Gene gene = entry.getValue();

            if (lockedCourseIds.contains(courseId)) {
                genes.put(courseId, gene);
                continue;
            }

            if (rng.nextDouble() < mutationRate) {
                Feasibility f = feasibility.getFeasibility(courseId);
                if (f != null && !f.validStartDates.isEmpty()) {
                    LocalDate newDate = f.validStartDates.get(rng.nextInt(f.validStartDates.size()));
                    Integer venueId = gene.getVenueId();
                    genes.put(courseId, gene.copyWith(newDate, venueId));
                    continue;
                }
            }
            genes.put(courseId, gene);
        }
        return new Chromosome(genes);
    }
}
