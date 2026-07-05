package com.wateracademy.scheduler.ga;

import java.time.LocalDate;
import java.util.Objects;

public final class Gene {

    private final int courseId;
    private final int trainerId;
    private final LocalDate startDate;
    private final Integer venueId;

    public Gene(int courseId, int trainerId, LocalDate startDate, Integer venueId) {
        this.courseId = courseId;
        this.trainerId = trainerId;
        this.startDate = startDate;
        this.venueId = venueId;
    }

    public int getCourseId() { return courseId; }
    public int getTrainerId() { return trainerId; }
    public LocalDate getStartDate() { return startDate; }
    public Integer getVenueId() { return venueId; }

    public boolean isScheduled() { return startDate != null; }

    public Gene copyWith(LocalDate newStartDate, Integer newVenueId) {
        return new Gene(courseId, trainerId,
                newStartDate != null ? newStartDate : this.startDate,
                newVenueId != null ? newVenueId : this.venueId);
    }

    public LocalDate endDate(int durationDays) {
        if (startDate == null) return null;
        return startDate.plusDays(durationDays - 1L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene)) return false;
        Gene g = (Gene) o;
        return courseId == g.courseId
                && trainerId == g.trainerId
                && Objects.equals(startDate, g.startDate)
                && Objects.equals(venueId, g.venueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, trainerId, startDate, venueId);
    }

    @Override
    public String toString() {
        return "Gene{course=" + courseId
                + ", trainer=" + trainerId
                + ", start=" + (startDate == null ? "null" : startDate)
                + ", venue=" + (venueId == null ? "null" : venueId)
                + '}';
    }
}
