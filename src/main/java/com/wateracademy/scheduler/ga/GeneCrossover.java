package com.wateracademy.scheduler.ga;

import org.uncommons.watchmaker.framework.operators.AbstractCrossover;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GeneCrossover extends AbstractCrossover<Chromosome> {

    private final Set<Integer> lockedCourseIds;

    public GeneCrossover(Set<Integer> lockedCourseIds,
                         org.uncommons.maths.random.Probability crossoverProbability) {
        super(1, crossoverProbability);
        this.lockedCourseIds = new HashSet<>(lockedCourseIds);
    }

    @Override
    protected List<Chromosome> mate(Chromosome parent1, Chromosome parent2, int numberOfCrossoverPoints, Random rng) {
        List<Integer> allIds = new ArrayList<>();
        for (var e : parent1.orderedEntries()) {
            allIds.add(e.getKey());
        }

        Set<Integer> donorIds = new HashSet<>();
        for (int id : allIds) {
            if (lockedCourseIds.contains(id)) continue;
            if (rng.nextBoolean()) {
                donorIds.add(id);
            }
        }

        Chromosome offspring1 = parent1.crossoverWith(parent2, donorIds);
        Chromosome offspring2 = parent2.crossoverWith(parent1, donorIds);
        return List.of(offspring1, offspring2);
    }
}
