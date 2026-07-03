package com.wateracademy.dto.request;

public record WorkspaceRequest(
    String name,
    String description,
    Integer year,
    String color
) {}
