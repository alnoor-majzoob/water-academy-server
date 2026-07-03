package com.wateracademy.dto.mapper;

import com.wateracademy.dto.request.CourseRequest;
import com.wateracademy.dto.response.CourseResponse;
import com.wateracademy.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toResponse(Course entity) {
        return new CourseResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getExternalId(),
            entity.getName(),
            entity.getSpecialization(),
            entity.getDurationDays(),
            entity.getHoursPerDay(),
            entity.getExpectedTrainees(),
            entity.getCity(),
            entity.getBeneficiary(),
            entity.getPriority(),
            entity.getType(),
            entity.getEarliestStart(),
            entity.getLatestEnd(),
            entity.getFixedDate(),
            entity.getNotes(),
            entity.getColor(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public Course toEntity(CourseRequest request) {
        var course = new Course();
        applyRequest(course, request);
        return course;
    }

    public void updateEntity(Course entity, CourseRequest request) {
        applyRequest(entity, request);
    }

    private void applyRequest(Course course, CourseRequest request) {
        course.setName(request.name());
        course.setSpecialization(request.specialization());
        course.setDurationDays(request.durationDays());
        course.setHoursPerDay(request.hoursPerDay());
        course.setExpectedTrainees(request.expectedTrainees());
        course.setCity(request.city());
        course.setBeneficiary(request.beneficiary());
        course.setPriority(request.priority());
        course.setType(request.type());
        course.setEarliestStart(request.earliestStart());
        course.setLatestEnd(request.latestEnd());
        course.setFixedDate(request.fixedDate());
        course.setNotes(request.notes());
        course.setColor(request.color());
    }
}