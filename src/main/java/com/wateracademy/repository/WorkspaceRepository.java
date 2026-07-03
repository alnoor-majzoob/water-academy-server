package com.wateracademy.repository;

import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.WorkspaceStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    List<Workspace> findByStatus(WorkspaceStatus status);
}
