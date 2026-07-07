package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.WorkspaceStatus;
import jakarta.validation.constraints.NotNull;

public record WorkspaceStatusRequest(
    @NotNull WorkspaceStatus status
) {}
