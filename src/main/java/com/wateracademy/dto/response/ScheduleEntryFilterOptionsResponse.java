package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.ScheduleStatus;
import java.util.List;

public record ScheduleEntryFilterOptionsResponse(
    List<String> cities,
    List<ScheduleStatus> statuses,
    List<String> months,
    List<Boolean> hasConflicts
) {
}
