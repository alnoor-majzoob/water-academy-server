package com.wateracademy.scheduler.ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Chromosome {

    private final Map<Integer, Gene> genes;
    private final List<Integer> sortedCourseIds;

    public Chromosome(Map<Integer, Gene> genes) {
        Objects.requireNonNull(genes, "genes");
        Map<Integer, Gene> copy = new LinkedHashMap<>(genes.size() * 2);
        for (Map.Entry<Integer, Gene> e : genes.entrySet()) {
            if (e.getValue() != null && e.getValue().getCourseId() != e.getKey().intValue()) {
                throw new IllegalArgumentException(
                        "Gene.courseId (" + e.getValue().getCourseId()
                                + ") does not match map key (" + e.getKey()
                                + "). The keyed design prevents this bug at runtime.");
            }
            copy.put(e.getKey(), e.getValue());
        }
        this.genes = Collections.unmodifiableMap(copy);
        List<Integer> sorted = new ArrayList<>(copy.keySet());
        Collections.sort(sorted);
        this.sortedCourseIds = Collections.unmodifiableList(sorted);
    }

    public Map<Integer, Gene> getGenes() { return genes; }

    public Gene geneFor(int courseId) { return genes.get(courseId); }

    public int length() { return genes.size(); }

    public Iterable<Map.Entry<Integer, Gene>> orderedEntries() {
        List<Map.Entry<Integer, Gene>> entries = new ArrayList<>(sortedCourseIds.size());
        for (Integer id : sortedCourseIds) {
            entries.add(new java.util.AbstractMap.SimpleEntry<>(id, genes.get(id)));
        }
        return entries;
    }

    public Chromosome withGene(int courseId, Gene updatedGene) {
        if (updatedGene == null || updatedGene.getCourseId() != courseId) {
            throw new IllegalArgumentException(
                    "withGene: gene.courseId does not match the requested key.");
        }
        Map<Integer, Gene> next = new LinkedHashMap<>(genes);
        next.put(courseId, updatedGene);
        return new Chromosome(next);
    }

    public Chromosome crossoverWith(Chromosome donor, java.util.Set<Integer> donorCourseIds) {
        Map<Integer, Gene> next = new LinkedHashMap<>(genes);
        for (Integer id : donorCourseIds) {
            Gene donorGene = donor.genes.get(id);
            if (donorGene == null) {
                throw new IllegalArgumentException(
                        "crossoverWith: donor has no gene for courseId " + id);
            }
            next.put(id, donorGene);
        }
        return new Chromosome(next);
    }

    public String signature() {
        StringBuilder sb = new StringBuilder();
        for (Integer id : sortedCourseIds) {
            Gene g = genes.get(id);
            sb.append(id).append(':');
            sb.append(g.getStartDate() == null ? "X" : g.getStartDate()).append(':');
            sb.append(g.getVenueId() == null ? "X" : g.getVenueId()).append('|');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chromosome)) return false;
        return Objects.equals(signature(), ((Chromosome) o).signature());
    }

    @Override
    public int hashCode() {
        return signature().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Chromosome{");
        boolean first = true;
        for (Map.Entry<Integer, Gene> e : orderedEntries()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(e.getValue());
        }
        sb.append('}');
        return sb.toString();
    }
}
