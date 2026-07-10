package com.wateracademy.dto.response;

import java.util.List;

public record TrainerFilterOptionsResponse(
    List<String> cities,
    List<String> trainerTypes,
    List<String> specialties
) {
}
