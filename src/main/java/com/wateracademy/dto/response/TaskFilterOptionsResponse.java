package com.wateracademy.dto.response;

import com.wateracademy.entity.enums.TaskStatus;
import java.util.List;

public record TaskFilterOptionsResponse(
    List<TaskStatus> statuses,
    List<String> types
) {
}
