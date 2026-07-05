package com.wateracademy.scheduler.ga;

public final class UnschedulableCourse {

    public final int courseId;
    public final String courseName;
    public final String reason;

    public UnschedulableCourse(int courseId, String courseName, String reason) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "[" + courseId + "] " + courseName + " -> " + reason;
    }
}
