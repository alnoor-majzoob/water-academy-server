package com.wateracademy.scheduler.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Calendar {

    private final Set<LocalDate> workingDays;
    private final Set<LocalDate> holidays;

    public Calendar(Set<LocalDate> workingDays, Set<LocalDate> holidays) {
        this.workingDays = Collections.unmodifiableSet(
                new HashSet<>(Objects.requireNonNull(workingDays, "workingDays")));
        this.holidays = Collections.unmodifiableSet(
                new HashSet<>(Objects.requireNonNullElse(holidays, Collections.emptySet())));
    }

    public Set<LocalDate> getWorkingDays() { return workingDays; }
    public Set<LocalDate> getHolidays() { return holidays; }

    public boolean isHoliday(LocalDate d) { return holidays.contains(d); }
    public boolean isWorkingDay(LocalDate d) { return workingDays.contains(d); }
}
