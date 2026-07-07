package com.wateracademy.repository;

import com.wateracademy.entity.Venue;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    @EntityGraph(attributePaths = {"workspace"})
    List<Venue> findByWorkspaceId(Long workspaceId);
}
