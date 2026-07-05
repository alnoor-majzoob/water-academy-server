package com.wateracademy.scheduler.model;

import java.time.LocalDate;
import java.util.Objects;

public final class Course {

    private final int id;
    private final String name;
    private final String specialization;
    private final int durationDays;
    private final int expectedTrainees;
    private final String preferredCity;
    private final String beneficiary;
    private final int priority;
    private final DeliveryType deliveryType;
    private final int trainerId;
    private final CourseStatus status;
    private final LocalDate lockedStartDate;
    private final Integer lockedVenueId;

    public Course(int id,
                  String name,
                  String specialization,
                  int durationDays,
                  int expectedTrainees,
                  String preferredCity,
                  String beneficiary,
                  int priority,
                  DeliveryType deliveryType,
                  int trainerId,
                  CourseStatus status) {
        this(id, name, specialization, durationDays, expectedTrainees, preferredCity,
                beneficiary, priority, deliveryType, trainerId, status, null, null);
    }

    public Course(int id,
                  String name,
                  String specialization,
                  int durationDays,
                  int expectedTrainees,
                  String preferredCity,
                  String beneficiary,
                  int priority,
                  DeliveryType deliveryType,
                  int trainerId,
                  CourseStatus status,
                  LocalDate lockedStartDate,
                  Integer lockedVenueId) {
        if (durationDays <= 0) {
            throw new IllegalArgumentException("durationDays must be > 0");
        }
        if (expectedTrainees <= 0) {
            throw new IllegalArgumentException("expectedTrainees must be > 0");
        }
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.specialization = Objects.requireNonNull(specialization, "specialization");
        this.durationDays = durationDays;
        this.expectedTrainees = expectedTrainees;
        this.preferredCity = Objects.requireNonNull(preferredCity, "preferredCity");
        this.beneficiary = Objects.requireNonNull(beneficiary, "beneficiary");
        this.priority = priority;
        this.deliveryType = Objects.requireNonNull(deliveryType, "deliveryType");
        this.trainerId = trainerId;
        this.status = status == null ? CourseStatus.PENDING : status;
        this.lockedStartDate = lockedStartDate;
        this.lockedVenueId = lockedVenueId;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public int getDurationDays() { return durationDays; }
    public int getExpectedTrainees() { return expectedTrainees; }
    public String getPreferredCity() { return preferredCity; }
    public String getBeneficiary() { return beneficiary; }
    public int getPriority() { return priority; }
    public DeliveryType getDeliveryType() { return deliveryType; }
    public int getTrainerId() { return trainerId; }
    public CourseStatus getStatus() { return status; }
    public LocalDate getLockedStartDate() { return lockedStartDate; }
    public Integer getLockedVenueId() { return lockedVenueId; }

    public boolean isLocked() {
        return status == CourseStatus.CONFIRMED || status == CourseStatus.EXECUTED;
    }

    @Override
    public String toString() {
        return "Course{id=" + id + ", name='" + name + "', priority=" + priority
                + ", status=" + status + "}";
    }
}
