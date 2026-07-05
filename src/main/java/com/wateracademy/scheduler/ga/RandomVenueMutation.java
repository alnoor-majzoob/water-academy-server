package com.wateracademy.scheduler.ga;

import com.wateracademy.scheduler.model.DeliveryType;

import java.util.List;
import java.util.Random;
import java.util.Set;

public final class RandomVenueMutation extends BaseGeneMutation {

    public RandomVenueMutation(FeasibilityProvider feasibility,
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

            if (gene.getStartDate() == null) {
                genes.put(courseId, gene);
                continue;
            }

            if (rng.nextDouble() < mutationRate) {
                Feasibility f = feasibility.getFeasibility(courseId);
                if (f != null && !f.compatibleVenues.isEmpty()) {
                    int idx = rng.nextInt(f.compatibleVenues.size());
                    Integer newVenueId = f.compatibleVenues.get(idx).getId();
                    genes.put(courseId, gene.copyWith(null, newVenueId));
                    continue;
                }
            }
            genes.put(courseId, gene);
        }
        return new Chromosome(genes);
    }
}
