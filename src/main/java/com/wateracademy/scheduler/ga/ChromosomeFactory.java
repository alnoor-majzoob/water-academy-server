package com.wateracademy.scheduler.ga;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class ChromosomeFactory extends AbstractCandidateFactory<Chromosome> {

    private final PopulationInitializer initializer;

    public ChromosomeFactory(PopulationInitializer initializer) {
        this.initializer = initializer;
    }

    @Override
    public Chromosome generateRandomCandidate(Random rng) {
        Map<Integer, Gene> genes = new HashMap<>();
        for (var course : initializer.getPrioritySortedCourses()) {
            if (course.isLocked()) {
                genes.put(course.getId(), initializer.lockedGeneFor(course));
                continue;
            }
            Gene g = initializer.randomGeneForCourse(course, rng);
            if (g == null) {
                genes.put(course.getId(),
                        new Gene(course.getId(), course.getTrainerId(), null, null));
            } else {
                genes.put(course.getId(), g);
            }
        }
        return new Chromosome(genes);
    }
}
