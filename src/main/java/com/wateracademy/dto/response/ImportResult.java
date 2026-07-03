package com.wateracademy.dto.response;

public record ImportResult(
    int coursesParsed,
    int coursesInserted,
    int trainersParsed,
    int trainersInserted,
    int venuesParsed,
    int venuesInserted,
    int calendarDaysParsed,
    int calendarDaysInserted,
    int assignmentsParsed,
    int assignmentsInserted,
    String error
) {
    public boolean hasError() {
        return error != null;
    }
}