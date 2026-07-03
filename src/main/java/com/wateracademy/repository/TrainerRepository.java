package com.wateracademy.repository;

import com.wateracademy.entity.Trainer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
    List<Trainer> findByWorkspaceId(UUID workspaceId);
}
