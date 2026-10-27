package com.techflow.task.service;

import com.techflow.common.exception.BusinessRuleViolationException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.project.model.Project;
import com.techflow.project.repository.ProjectRepository;
import com.techflow.task.dto.CreateTaskRequest;
import com.techflow.task.dto.TaskResponse;
import com.techflow.task.dto.UpdateTaskRequest;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.RecurrenceType;
import com.techflow.task.model.Tag;
import com.techflow.task.model.Task;
import com.techflow.task.model.TaskPriority;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.repository.TagRepository;
import com.techflow.task.repository.TaskRepository;
import com.techflow.user.model.User;
import com.techflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final int MAX_WIP_LIMIT = 10;

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(UUID userId, UUID projectId, TaskStatus status, TaskPriority priority, String tag, String search, Instant deadlineFrom, Instant deadlineTo, Pageable pageable) {
        Page<Task> tasks = taskRepository.searchTasks(userId, status, priority, projectId, tag, search, deadlineFrom, deadlineTo, pageable);
        return tasks.map(taskMapper::toTaskResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID userId, UUID taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
        return taskMapper.toTaskResponse(task);
    }

    @Transactional
    public TaskResponse createTask(UUID userId, CreateTaskRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findByIdAndUserId(request.getProjectId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
        }

        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            tags = new HashSet<>(tagRepository.findByIdInAndUserId(request.getTagIds(), userId));
        }

        Task task = Task.builder()
                .user(user)
                .project(project)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .deadline(request.getDeadline())
                .recurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : RecurrenceType.NONE)
                .tags(tags)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Created task {} for user {}", saved.getId(), userId);
        return taskMapper.toTaskResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(UUID userId, UUID taskId, UpdateTaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndUserId(request.getProjectId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
            task.setProject(project);
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle().trim());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }

        if (request.getRecurrenceType() != null) {
            task.setRecurrenceType(request.getRecurrenceType());
        }

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findByIdInAndUserId(request.getTagIds(), userId));
            task.setTags(tags);
        }

        // BR-TASK-002: Kiểm tra WIP limit nếu chuyển sang IN_PROGRESS
        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            if (request.getStatus() == TaskStatus.IN_PROGRESS) {
                checkWipLimit(userId);
            }
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
            } else if (task.getStatus() == TaskStatus.COMPLETED) {
                task.setCompletedAt(null);
            }
            task.setStatus(request.getStatus());
        }

        Task saved = taskRepository.save(task);
        log.info("Updated task {} for user {}", saved.getId(), userId);
        return taskMapper.toTaskResponse(saved);
    }

    @Transactional
    public void deleteTask(UUID userId, UUID taskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
        log.info("Deleted task {} for user {}", taskId, userId);
    }

    private void checkWipLimit(UUID userId) {
        long inProgressCount = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        if (inProgressCount >= MAX_WIP_LIMIT) {
            throw new BusinessRuleViolationException("WIP_LIMIT_EXCEEDED",
                    "WIP limit exceeded: user already has " + inProgressCount + " tasks IN_PROGRESS (max: " + MAX_WIP_LIMIT + ")");
        }
    }
}