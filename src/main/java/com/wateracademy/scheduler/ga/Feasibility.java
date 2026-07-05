package com.wateracademy.scheduler.ga;

import com.wateracademy.scheduler.model.Venue;

import java.time.LocalDate;
import java.util.List;

public final class Feasibility {

    public final int courseId;
    public final List<LocalDate> validStartDates;
    public final List<Venue> compatibleVenues;

    public Feasibility(int courseId,
                        List<LocalDate> validStartDates,
                        List<Venue> compatibleVenues) {
        this.courseId = courseId;
        this.validStartDates = List.copyOf(validStartDates);
        this.compatibleVenues = List.copyOf(compatibleVenues);
    }
}
