package com.wateracademy.scheduler.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Trainer {

    private final int id;
    private final String name;
    private final String city;
    private final Set<LocalDate> unavailableDates;

    public Trainer(int id,
                   String name,
                   String city,
                   Set<LocalDate> unavailableDates) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.city = Objects.requireNonNull(city, "city");
        this.unavailableDates = unavailableDates == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(unavailableDates));
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public Set<LocalDate> getUnavailableDates() { return unavailableDates; }

    @Override
    public String toString() {
        return "Trainer{id=" + id + ", name='" + name + "'}";
    }
}
