package com.wateracademy.service;

import com.wateracademy.dto.mapper.CalendarDayMapper;
import com.wateracademy.dto.request.CalendarDayRequest;
import com.wateracademy.dto.response.CalendarDayResponse;
import com.wateracademy.entity.CalendarDay;
import com.wateracademy.exception.DuplicateResourceException;
import com.wateracademy.exception.ResourceNotFoundException;
import com.wateracademy.repository.CalendarDayRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CalendarDayService {

    private final CalendarDayRepository repository;
    private final CalendarDayMapper mapper;
    private final WorkspaceService workspaceService;

    public CalendarDayService(CalendarDayRepository repository, CalendarDayMapper mapper,
                              WorkspaceService workspaceService) {
        this.repository = repository;
        this.mapper = mapper;
        this.workspaceService = workspaceService;
    }

    @Transactional(readOnly = true)
    public List<CalendarDayResponse> findAllByWorkspaceId(Long workspaceId) {
        return repository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CalendarDayResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    public CalendarDayResponse create(Long workspaceId, CalendarDayRequest request) {
        var workspace = workspaceService.findEntity(workspaceId);
        if (repository.findByWorkspaceIdAndDate(workspaceId, request.date()).isPresent()) {
            throw new DuplicateResourceException(
                    "Calendar day already exists for date " + request.date());
        }
        var entity = mapper.toEntity(request);
        entity.setWorkspace(workspace);
        return mapper.toResponse(repository.save(entity));
    }

    public List<CalendarDayResponse> bulkCreate(Long workspaceId, List<CalendarDayRequest> requests) {
        var workspace = workspaceService.findEntity(workspaceId);
        var entities = requests.stream()
                .map(req -> {
                    var entity = mapper.toEntity(req);
                    entity.setWorkspace(workspace);
                    return entity;
                })
                .toList();
        return repository.saveAll(entities).stream()
                .map(mapper::toResponse)
                .toList();
    }

    public CalendarDayResponse update(Long id, CalendarDayRequest request) {
        var entity = findEntity(id);
        entity.setDate(request.date());
        entity.setIsWorkDay(request.isWorkDay() != null ? request.isWorkDay() : entity.getIsWorkDay());
        entity.setIsHoliday(request.isHoliday() != null ? request.isHoliday() : entity.getIsHoliday());
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        var entity = findEntity(id);
        repository.delete(entity);
    }

    CalendarDay findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CalendarDay", id));
    }
}
