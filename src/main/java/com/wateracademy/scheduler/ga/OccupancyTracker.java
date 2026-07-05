package com.wateracademy.scheduler.ga;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OccupancyTracker {

    private final Map<Integer, Set<LocalDate>> trainerDays = new HashMap<>();
    private final Map<Integer, Set<LocalDate>> venueDays = new HashMap<>();

    public boolean isTrainerFree(int trainerId, List<LocalDate> days) {
        Set<LocalDate> taken = trainerDays.get(trainerId);
        if (taken == null) return true;
        for (LocalDate d : days) {
            if (taken.contains(d)) return false;
        }
        return true;
    }

    public boolean isVenueFree(int venueId, List<LocalDate> days) {
        Set<LocalDate> taken = venueDays.get(venueId);
        if (taken == null) return true;
        for (LocalDate d : days) {
            if (taken.contains(d)) return false;
        }
        return true;
    }

    public void reserveTrainer(int trainerId, List<LocalDate> days) {
        trainerDays.computeIfAbsent(trainerId, k -> new HashSet<>()).addAll(days);
    }

    public void reserveVenue(int venueId, List<LocalDate> days) {
        venueDays.computeIfAbsent(venueId, k -> new HashSet<>()).addAll(days);
    }
}
