package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.CourseType;
import java.util.List;

public record VenueFilterOptionsResponse(
    List<String> cities,
    List<CourseType> types
) {
}
