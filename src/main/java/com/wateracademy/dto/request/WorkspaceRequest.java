package com.wateracademy.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WorkspaceRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 5000) String description,
    @NotNull @Min(2000) @Max(2100) Integer year,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color
) {}
