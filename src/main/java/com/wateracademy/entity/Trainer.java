package com.wateracademy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "trainer", indexes = {
    @Index(columnList = "workspace_id")
})
public class Trainer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Workspace workspace;

    @Column(name = "external_id", length = 20)
    private String externalId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String specialties;

    private String city;

    @Column(name = "trainer_type", length = 20)
    private String trainerType;

    @Column(columnDefinition = "TEXT")
    private String unavailableDates;

    @Column(name = "max_days_per_month")
    private Integer maxDaysPerMonth;

    @Column(name = "max_consecutive_days")
    private Integer maxConsecutiveDays;

    @Column(name = "cost_per_day")
    private Integer costPerDay;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTrainerType() {
        return trainerType;
    }

    public void setTrainerType(String trainerType) {
        this.trainerType = trainerType;
    }

    public String getUnavailableDates() {
        return unavailableDates;
    }

    public void setUnavailableDates(String unavailableDates) {
        this.unavailableDates = unavailableDates;
    }

    public Integer getMaxDaysPerMonth() {
        return maxDaysPerMonth;
    }

    public void setMaxDaysPerMonth(Integer maxDaysPerMonth) {
        this.maxDaysPerMonth = maxDaysPerMonth;
    }

    public Integer getMaxConsecutiveDays() {
        return maxConsecutiveDays;
    }

    public void setMaxConsecutiveDays(Integer maxConsecutiveDays) {
        this.maxConsecutiveDays = maxConsecutiveDays;
    }

    public Integer getCostPerDay() {
        return costPerDay;
    }

    public void setCostPerDay(Integer costPerDay) {
        this.costPerDay = costPerDay;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
