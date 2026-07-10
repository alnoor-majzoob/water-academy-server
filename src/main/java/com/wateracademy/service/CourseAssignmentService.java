package com.wateracademy.service;

import com.wateracademy.dto.mapper.CourseAssignmentMapper;
import com.wateracademy.dto.request.CourseAssignmentRequest;
import com.wateracademy.dto.response.CourseAssignmentResponse;
import com.wateracademy.dto.response.PageResponse;
import com.wateracademy.entity.CourseAssignment;
import com.wateracademy.exception.DuplicateResourceException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CourseAssignmentRepository;
import com.wateracademy.util.PaginationUtils;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CourseAssignmentService {

    private static final Set<String> SORT_FIELDS = Set.of("id", "createdAt", "updatedAt");

    private final CourseAssignmentRepository repository;
    private final CourseAssignmentMapper mapper;
    private final WorkspaceService workspaceService;
    private final TrainerService trainerService;
    private final CourseService courseService;

    public CourseAssignmentService(CourseAssignmentRepository repository,
                                   CourseAssignmentMapper mapper,
                                   WorkspaceService workspaceService,
                                   TrainerService trainerService,
                                   CourseService courseService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
        this.trainerService = trainerService;
        this.courseService = courseService;
    }

    @Transactional(readOnly = true)
    public List<CourseAssignmentResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseAssignmentResponse> findPageByWorkspaceId(Long workspaceId, Integer page, Integer size,
                                                                        List<String> sort, Long courseId, Long trainerId,
                                                                        String search) {
        var pageable = PaginationUtils.pageable(page, size, sort, SORT_FIELDS, Sort.by("createdAt").descending());
        return PageResponse.from(repository.searchByWorkspaceId(
                workspaceId, courseId, trainerId, PaginationUtils.like(search), pageable).map(mapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CourseAssignmentResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public CourseAssignmentResponse create(Long workspaceId, CourseAssignmentRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        var trainer = trainerService.findEntity(request.trainerId());
        var course = courseService.findEntity(request.courseId());

        validateBelongsToWorkspace(workspaceId, trainer, course);

        if (repository.findByCourseId(request.courseId()).stream()
                .anyMatch(a -> a.getTrainer().getId().equals(request.trainerId()))) {
            throw new DuplicateResourceException("Trainer already assigned to this course");
        }

        var entity = new CourseAssignment();
        entity.setWorkspace(workspace);
        entity.setTrainer(trainer);
        entity.setCourse(course);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    CourseAssignment findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CourseAssignment", id));
    }

    private void validateBelongsToWorkspace(Long workspaceId,
                                            com.wateracademy.entity.Trainer trainer,
                                            com.wateracademy.entity.Course course) {
        if (!trainer.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Trainer", trainer.getId());
        }
        if (!course.getWorkspace().getId().equals(workspaceId)) {
            throw new ResourceNotFoundException("Course", course.getId());
        }
    }
}
