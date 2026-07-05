package com.wateracademy.scheduler;

import com.wateracademy.scheduler.ga.Chromosome;
import com.wateracademy.scheduler.ga.PopulationInitializer;
import com.wateracademy.scheduler.ga.ScheduleEngineFactory;
import com.wateracademy.scheduler.ga.ScheduleFitnessEvaluator;
import com.wateracademy.scheduler.model.Calendar;
import com.wateracademy.scheduler.model.Course;
import com.wateracademy.scheduler.model.Trainer;
import com.wateracademy.scheduler.model.Venue;
import org.uncommons.watchmaker.framework.EvolutionEngine;
import org.uncommons.watchmaker.framework.termination.GenerationCount;
import org.uncommons.watchmaker.framework.termination.TargetFitness;
import org.uncommons.watchmaker.framework.termination.UserAbort;

import java.util.List;
import java.util.Random;

public final class SchedulerService {

    public static final class Config {
        public int populationSize = 5;
        public int maxGenerations = 80;
        public double mutationRate = 0.05;
        public int offspringMultiplier = ScheduleEngineFactory.DEFAULT_OFFSPRING_MULTIPLIER;
        public double greedyRatio = 0.1;
        public double clusteredRatio = 0.1;
        public Long seed = 42L;
        public Double targetFitness = null;
    }

    private final List<Course> courses;
    private final List<Trainer> trainers;
    private final List<Venue> venues;
    private final Calendar calendar;
    private final Config config;

    public SchedulerService(List<Course> courses,
                            List<Trainer> trainers,
                            List<Venue> venues,
                            Calendar calendar,
                            Config config) {
        this.courses = courses;
        this.trainers = trainers;
        this.venues = venues;
        this.calendar = calendar;
        this.config = config;
    }

    public ScheduleReport run() {
        long t0 = System.nanoTime();

        PopulationInitializer initializer = new PopulationInitializer(
                courses, trainers, venues, calendar,
                config.populationSize, config.greedyRatio, config.clusteredRatio, config.seed);
        var unschedulable = initializer.getUnschedulable();

        List<Chromosome> seedPopulation = initializer.buildSeedPopulation();

        ScheduleFitnessEvaluator fitness = new ScheduleFitnessEvaluator(
                courses, trainers, venues, calendar);

        EvolutionEngine<Chromosome> engine = ScheduleEngineFactory.build(
                initializer, seedPopulation, fitness,
                config.mutationRate, config.offspringMultiplier,
                new Random(config.seed == null ? System.nanoTime() : config.seed + 1));

        TargetFitness tf = null;
        if (config.targetFitness != null) {
            tf = new TargetFitness(config.targetFitness, true);
        }
        Chromosome best;
        if (tf != null) {
            best = engine.evolve(config.populationSize, 0, seedPopulation,
                    new GenerationCount(config.maxGenerations), new UserAbort(), tf);
        } else {
            best = engine.evolve(config.populationSize, 0, seedPopulation,
                    new GenerationCount(config.maxGenerations), new UserAbort());
        }

        long elapsedMs = (System.nanoTime() - t0) / 1_000_000L;
        double bestFitness = fitness.getFitness(best, null);

        return ScheduleReport.fromChromosome(best, courses, trainers, venues, calendar, unschedulable,
                bestFitness, elapsedMs, config.populationSize, config.populationSize * config.offspringMultiplier);
    }
}
