package com.wateracademy.repository;

import com.wateracademy.entity.Venue;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
    List<Venue> findByWorkspaceId(UUID workspaceId);
}
