package com.wateracademy.dto.mapper;

import com.wateracademy.dto.request.CalendarDayRequest;
import com.wateracademy.dto.response.CalendarDayResponse;
import com.wateracademy.entity.CalendarDay;
import org.springframework.stereotype.Component;

@Component
public class CalendarDayMapper {

    public CalendarDayResponse toResponse(CalendarDay entity) {
        return new CalendarDayResponse(
            entity.getId(),
            entity.getWorkspace().getId(),
            entity.getDate(),
            entity.getIsWorkDay(),
            entity.getIsHoliday(),
            entity.getCreatedAt()
        );
    }

    public CalendarDay toEntity(CalendarDayRequest request) {
        var day = new CalendarDay();
        day.setDate(request.date());
        day.setIsWorkDay(request.isWorkDay() != null ? request.isWorkDay() : true);
        day.setIsHoliday(request.isHoliday() != null ? request.isHoliday() : false);
        return day;
    }
}
