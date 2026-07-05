package com.wateracademy.scheduler.ga;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class BaseGeneMutation implements EvolutionaryOperator<Chromosome> {

    protected final FeasibilityProvider feasibility;
    protected final Set<Integer> lockedCourseIds;
    protected final double mutationRate;

    public BaseGeneMutation(FeasibilityProvider feasibility,
                            Set<Integer> lockedCourseIds,
                            double mutationRate) {
        this.feasibility = feasibility;
        this.lockedCourseIds = lockedCourseIds;
        this.mutationRate = mutationRate;
    }

    @Override
    public List<Chromosome> apply(List<Chromosome> selectedCandidates, Random rng) {
        List<Chromosome> result = new ArrayList<>();
        for (Chromosome c : selectedCandidates) {
            result.add(mutate(c, rng));
        }
        return result;
    }

    protected abstract Chromosome mutate(Chromosome c, Random rng);

    public interface FeasibilityProvider {
        Feasibility getFeasibility(int courseId);
    }
}
