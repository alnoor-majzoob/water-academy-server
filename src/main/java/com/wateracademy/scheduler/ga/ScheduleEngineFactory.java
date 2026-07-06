package com.wateracademy.scheduler.ga;

import org.uncommons.maths.random.Probability;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.EvolutionStrategyEngine;
import org.uncommons.watchmaker.framework.operators.EvolutionPipeline;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ScheduleEngineFactory {

    public static final int DEFAULT_OFFSPRING_MULTIPLIER = 7;

    private ScheduleEngineFactory() { }

    public static EvolutionEngine<Chromosome> build(PopulationInitializer initializer,
                                                    List<Chromosome> seedPopulation,
                                                    ScheduleFitnessEvaluator fitness,
                                                    double mutationRate,
                                                    Random rng) {
        return build(initializer, seedPopulation, fitness, mutationRate,
                DEFAULT_OFFSPRING_MULTIPLIER, rng);
    }

    public static EvolutionEngine<Chromosome> build(PopulationInitializer initializer,
                                                    List<Chromosome> seedPopulation,
                                                    ScheduleFitnessEvaluator fitness,
                                                    double mutationRate,
                                                    int offspringMultiplier,
                                                    Random rng) {
        if (offspringMultiplier < 1) {
            throw new IllegalArgumentException(
                    "offspringMultiplier must be >= 1, got " + offspringMultiplier);
        }

        ChromosomeFactory factory = new ChromosomeFactory(initializer);
        GeneCrossover crossover = new GeneCrossover(initializer.lockedCourseIds(),
                new Probability(1.0));

        double perModeRate = mutationRate / 3.0;
        EvolutionPipeline<Chromosome> mutationPipeline = new EvolutionPipeline<>(Arrays.asList(
                new DateShiftMutation(initializer.getFeasibility()::get,
                        initializer.lockedCourseIds(), perModeRate, 10),
                new RandomDateMutation(initializer.getFeasibility()::get,
                        initializer.lockedCourseIds(), perModeRate),
                new RandomVenueMutation(initializer.getFeasibility()::get,
                        initializer.lockedCourseIds(), perModeRate)));

        EvolutionPipeline<Chromosome> evolutionScheme =
                new EvolutionPipeline<>(Arrays.asList(crossover, mutationPipeline));

        return new EvolutionStrategyEngine<>(
                factory,
                evolutionScheme,
                fitness,
                true,
                offspringMultiplier,
                rng);
    }

    public static EvolutionEngine<Chromosome> buildWithSeed(
            PopulationInitializer initializer,
            List<Chromosome> seedPopulation,
            ScheduleFitnessEvaluator fitness,
            double mutationRate,
            Random rng) {
        return build(initializer, seedPopulation, fitness, mutationRate, rng);
    }

    public static List<Chromosome> emptySeed() { return List.of(); }
}
