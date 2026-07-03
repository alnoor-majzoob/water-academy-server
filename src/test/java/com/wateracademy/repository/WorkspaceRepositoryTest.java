package com.wateracademy.repository;

import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.WorkspaceStatus;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class WorkspaceRepositoryTest {

    @Autowired
    private WorkspaceRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldSaveWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test Workspace");
        workspace.setYear(2026);
        workspace.setStatus(WorkspaceStatus.DRAFT);
        workspace.setColor("#FF5733");

        var saved = repository.save(workspace);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Workspace");
        assertThat(saved.getYear()).isEqualTo(2026);
        assertThat(saved.getStatus()).isEqualTo(WorkspaceStatus.DRAFT);
        assertThat(saved.getColor()).isEqualTo("#FF5733");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindByStatus() {
        var workspace = new Workspace();
        workspace.setName("Draft Workspace");
        workspace.setYear(2026);
        workspace.setStatus(WorkspaceStatus.DRAFT);
        em.persist(workspace);

        var results = repository.findByStatus(WorkspaceStatus.DRAFT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Draft Workspace");
    }

    @Test
    void shouldUpdateStatus() {
        var workspace = new Workspace();
        workspace.setName("Updatable");
        workspace.setYear(2026);
        workspace.setStatus(WorkspaceStatus.DRAFT);
        em.persist(workspace);

        workspace.setStatus(WorkspaceStatus.OPTIMIZED);
        repository.save(workspace);

        var found = repository.findById(workspace.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(WorkspaceStatus.OPTIMIZED);
    }

    @Test
    void shouldReturnEmptyWhenNoMatchingStatus() {
        var results = repository.findByStatus(WorkspaceStatus.DISABLED);
        assertThat(results).isEmpty();
    }
}
