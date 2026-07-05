package com.wateracademy.repository;

import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.WorkspaceStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    List<Workspace> findByStatus(WorkspaceStatus status);
}
