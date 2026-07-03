package com.wateracademy.dto.request;

import com.wateracademy.entity.enums.WorkspaceStatus;

public record WorkspaceStatusRequest(
    WorkspaceStatus status
) {}
