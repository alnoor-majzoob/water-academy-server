package com.wateracademy.dto.mapper;

import com.wateracademy.dto.response.CourseAssignmentResponse;
import com.wateracademy.entity.CourseAssignment;
import org.springframework.stereotype.Component;

@Component
public class CourseAssignmentMapper {

    public CourseAssignmentResponse toResponse(CourseAssignment entity) {
        return new CourseAssignmentResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getTrainer().getId(),
            entity.getCourse().getId(),
            entity.getCreatedAt()
        );
    }
}
