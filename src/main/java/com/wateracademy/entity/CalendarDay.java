package com.wateracademy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "calendar_day", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "date"})
})
public class CalendarDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "is_work_day", nullable = false)
    private Boolean isWorkDay = true;

    @Column(name = "is_holiday", nullable = false)
    private Boolean isHoliday = false;

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Boolean getIsWorkDay() {
        return isWorkDay;
    }

    public void setIsWorkDay(Boolean isWorkDay) {
        this.isWorkDay = isWorkDay;
    }

    public Boolean getIsHoliday() {
        return isHoliday;
    }

    public void setIsHoliday(Boolean isHoliday) {
        this.isHoliday = isHoliday;
    }
}
