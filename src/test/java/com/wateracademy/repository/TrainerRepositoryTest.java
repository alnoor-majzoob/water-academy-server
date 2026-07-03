package com.wateracademy.repository;

import com.wateracademy.entity.Trainer;
import com.wateracademy.entity.Workspace;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainerRepositoryTest {

    @Autowired
    private TrainerRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    @Test
    void shouldSaveTrainer() {
        var workspace = persistWorkspace();

        var trainer = new Trainer();
        trainer.setWorkspace(workspace);
        trainer.setName("Dr. Ahmed");
        trainer.setCity("Khartoum");

        var saved = repository.save(trainer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Dr. Ahmed");
        assertThat(saved.getCity()).isEqualTo("Khartoum");
    }

    @Test
    void shouldFindByWorkspaceId() {
        var workspace = persistWorkspace();

        var trainer = new Trainer();
        trainer.setWorkspace(workspace);
        trainer.setName("Dr. Sara");
        em.persist(trainer);

        List<Trainer> results = repository.findByWorkspaceId(workspace.getId());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Dr. Sara");
    }

    @Test
    void shouldReturnEmptyForUnknownWorkspace() {
        var results = repository.findByWorkspaceId(java.util.UUID.randomUUID());
        assertThat(results).isEmpty();
    }
}
