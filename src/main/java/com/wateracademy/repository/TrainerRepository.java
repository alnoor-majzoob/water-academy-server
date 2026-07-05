package com.wateracademy.repository;

import com.wateracademy.entity.Trainer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    List<Trainer> findByWorkspaceId(Long workspaceId);
}
