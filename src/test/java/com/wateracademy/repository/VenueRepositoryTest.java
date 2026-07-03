package com.wateracademy.repository;

import com.wateracademy.entity.Venue;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.CourseType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VenueRepositoryTest {

    @Autowired
    private VenueRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    @Test
    void shouldSaveVenue() {
        var workspace = persistWorkspace();

        var venue = new Venue();
        venue.setWorkspace(workspace);
        venue.setName("Main Hall");
        venue.setCity("Khartoum");
        venue.setCapacity(50);
        venue.setType(CourseType.IN_PERSON);

        var saved = repository.save(venue);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Main Hall");
        assertThat(saved.getCapacity()).isEqualTo(50);
    }

    @Test
    void shouldFindByWorkspaceId() {
        var workspace = persistWorkspace();

        var venue = new Venue();
        venue.setWorkspace(workspace);
        venue.setName("Room A");
        venue.setCapacity(30);
        venue.setType(CourseType.IN_PERSON);
        em.persist(venue);

        List<Venue> results = repository.findByWorkspaceId(workspace.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Room A");
    }

    @Test
    void shouldReturnEmptyForUnknownWorkspace() {
        var results = repository.findByWorkspaceId(java.util.UUID.randomUUID());
        assertThat(results).isEmpty();
    }
}
