package com.wateracademy.entity;

import com.wateracademy.entity.enums.CourseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "course")
public class Course extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "external_id", length = 20)
    private String externalId;

    @Column(nullable = false)
    private String name;

    private String specialization;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "hours_per_day")
    private Integer hoursPerDay;

    @Column(name = "expected_trainees")
    private Integer expectedTrainees;

    private String city;

    private String beneficiary;

    @Column(length = 20)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CourseType type = CourseType.IN_PERSON;

    @Column(name = "earliest_start")
    private LocalDate earliestStart;

    @Column(name = "latest_end")
    private LocalDate latestEnd;

    @Column(name = "fixed_date")
    private LocalDate fixedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 7)
    private String color;

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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public Integer getHoursPerDay() {
        return hoursPerDay;
    }

    public void setHoursPerDay(Integer hoursPerDay) {
        this.hoursPerDay = hoursPerDay;
    }

    public Integer getExpectedTrainees() {
        return expectedTrainees;
    }

    public void setExpectedTrainees(Integer expectedTrainees) {
        this.expectedTrainees = expectedTrainees;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public LocalDate getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(LocalDate earliestStart) {
        this.earliestStart = earliestStart;
    }

    public LocalDate getLatestEnd() {
        return latestEnd;
    }

    public void setLatestEnd(LocalDate latestEnd) {
        this.latestEnd = latestEnd;
    }

    public LocalDate getFixedDate() {
        return fixedDate;
    }

    public void setFixedDate(LocalDate fixedDate) {
        this.fixedDate = fixedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
