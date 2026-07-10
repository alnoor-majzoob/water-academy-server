package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.CourseType;
import java.util.List;

public record CourseFilterOptionsResponse(
    List<String> cities,
    List<CourseType> types,
    List<String> priorities,
    List<String> specializations
) {
}
