package com.wateracademy.scheduler.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class Venue {

    private final int id;
    private final String name;
    private final String city;
    private final int capacity;
    private final VenueType type;
    private final Set<LocalDate> unavailableDates;

    public Venue(int id,
                 String name,
                 String city,
                 int capacity,
                 VenueType type,
                 Set<LocalDate> unavailableDates) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.city = Objects.requireNonNull(city, "city");
        this.capacity = capacity;
        this.type = Objects.requireNonNull(type, "type");
        this.unavailableDates = unavailableDates == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(unavailableDates));
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getCapacity() { return capacity; }
    public VenueType getType() { return type; }
    public Set<LocalDate> getUnavailableDates() { return unavailableDates; }

    @Override
    public String toString() {
        return "Venue{id=" + id + ", name='" + name + "', city='" + city + "'}";
    }
}
