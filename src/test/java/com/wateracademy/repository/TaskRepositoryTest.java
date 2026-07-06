package com.wateracademy.repository;

import com.wateracademy.entity.Task;
import com.wateracademy.entity.Workspace;
import com.wateracademy.entity.enums.TaskStatus;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private TestEntityManager em;

    private Workspace persistWorkspace() {
        var workspace = new Workspace();
        workspace.setName("Test WS");
        workspace.setYear(2026);
        return em.persist(workspace);
    }

    @Test
    void shouldSaveTask() {
        var workspace = persistWorkspace();

        var task = new Task();
        task.setWorkspace(workspace);
        task.setStatus(TaskStatus.PENDING);
        task.setMode("new");
        task.setLog("Starting...");

        var saved = repository.save(task);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(saved.getLog()).isEqualTo("Starting...");
    }

    @Test
    void shouldFindByWorkspaceAndStatus() {
        var workspace = persistWorkspace();

        var task = new Task();
        task.setWorkspace(workspace);
        task.setStatus(TaskStatus.RUNNING);
        em.persist(task);

        Optional<Task> found = repository.findByWorkspaceIdAndStatus(workspace.getId(), TaskStatus.RUNNING);

        assertThat(found).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenNoTaskWithStatus() {
        var workspace = persistWorkspace();

        Optional<Task> found = repository.findByWorkspaceIdAndStatus(workspace.getId(), TaskStatus.RUNNING);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckExistsByWorkspaceAndStatus() {
        var workspace = persistWorkspace();

        var task = new Task();
        task.setWorkspace(workspace);
        task.setStatus(TaskStatus.RUNNING);
        em.persist(task);

        boolean exists = repository.existsByWorkspaceIdAndStatus(workspace.getId(), TaskStatus.RUNNING);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoRunningTask() {
        var workspace = persistWorkspace();

        boolean exists = repository.existsByWorkspaceIdAndStatus(workspace.getId(), TaskStatus.RUNNING);
        assertThat(exists).isFalse();
    }

    @Test
    void shouldTrackTimestamps() {
        var workspace = persistWorkspace();

        var task = new Task();
        task.setWorkspace(workspace);
        task.setStatus(TaskStatus.RUNNING);
        em.persistAndFlush(task);

        task.setStatus(TaskStatus.COMPLETED);
        em.persistAndFlush(task);

        var updated = repository.findById(task.getId()).orElseThrow();
        assertThat(updated.getUpdatedAt()).isAfter(updated.getCreatedAt());
    }
}
