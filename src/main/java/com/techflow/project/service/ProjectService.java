package com.techflow.project.service;

import com.techflow.common.exception.ConflictException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.common.response.ApiResponse;
import com.techflow.common.response.PageMeta;
import com.techflow.project.dto.CreateProjectRequest;
import com.techflow.project.dto.ProjectResponse;
import com.techflow.project.dto.UpdateProjectRequest;
import com.techflow.project.mapper.ProjectMapper;
import com.techflow.project.model.Project;
import com.techflow.project.repository.ProjectRepository;
import com.techflow.task.model.Task;
import com.techflow.task.repository.TaskRepository;
import com.techflow.user.model.User;
import com.techflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(UUID userId, CreateProjectRequest request) {
        log.info("Creating project '{}' for user {}", request.getName(), userId);

        if (projectRepository.existsByNameIgnoreCaseAndUserId(request.getName(), userId)) {
            throw new ConflictException("PROJECT_NAME_DUPLICATE", "Project name already exists: " + request.getName());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .build();

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ProjectResponse>> getProjects(UUID userId, Pageable pageable) {
        Page<Project> page = projectRepository.findByUserId(userId, pageable);
        List<ProjectResponse> content = page.getContent().stream()
                .map(projectMapper::toResponse)
                .toList();

        PageMeta meta = PageMeta.builder()
                .page(page.getNumber())
                .limit(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ApiResponse.success(content, meta);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID userId, UUID projectId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (request.getName() != null && !request.getName().equalsIgnoreCase(project.getName())) {
            if (projectRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(request.getName(), userId, projectId)) {
                throw new ConflictException("PROJECT_NAME_DUPLICATE", "Project name already exists: " + request.getName());
            }
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        Project updated = projectRepository.save(project);
        return projectMapper.toResponse(updated);
    }

    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // BR-PROJECT-002: Set project_id = null on related tasks
        List<Task> relatedTasks = taskRepository.findByProjectId(projectId);
        for (Task task : relatedTasks) {
            task.setProject(null);
        }
        taskRepository.saveAll(relatedTasks);

        projectRepository.delete(project);
        log.info("Project {} deleted and tasks unassigned", projectId);
    }
}