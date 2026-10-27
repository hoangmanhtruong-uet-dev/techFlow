package com.techflow.task;

import com.techflow.common.exception.BusinessRuleViolationException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.project.model.Project;
import com.techflow.project.repository.ProjectRepository;
import com.techflow.task.dto.CreateTaskRequest;
import com.techflow.task.dto.TaskResponse;
import com.techflow.task.dto.UpdateTaskRequest;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.Task;
import com.techflow.task.model.TaskPriority;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.repository.TagRepository;
import com.techflow.task.repository.TaskRepository;
import com.techflow.task.service.TaskService;
import com.techflow.user.model.User;
import com.techflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    private UUID userAId;
    private UUID userBId;
    private UUID taskId;
    private UUID projectId;
    private User userA;
    private Task task;
    private Project projectA;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        userAId = UUID.randomUUID();
        userBId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        userA = User.builder().id(userAId).email("usera@example.com").build();
        projectA = Project.builder().id(projectId).user(userA).name("Project A").build();

        task = Task.builder()
                .id(taskId)
                .user(userA)
                .project(projectA)
                .title("Sample Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .tags(Set.of())
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Sample Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();
    }

    @Test
    @DisplayName("CreateTask - Thành công với dữ liệu hợp lệ")
    void createTask_Success() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setProjectId(projectId);
        request.setPriority(TaskPriority.HIGH);

        when(userRepository.findById(userAId)).thenReturn(Optional.of(userA));
        when(projectRepository.findByIdAndUserId(projectId, userAId)).thenReturn(Optional.of(projectA));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toTaskResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.createTask(userAId, request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Sample Task");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("CreateTask - BR-AUTH-001: Chặn khi user A gán Task vào Project của user B")
    void createTask_AssignOtherUsersProject_ThrowsResourceNotFoundException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task for User A");
        request.setProjectId(projectId);

        when(userRepository.findById(userAId)).thenReturn(Optional.of(userA));
        when(projectRepository.findByIdAndUserId(projectId, userAId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(userAId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("GetTaskById - BR-AUTH-001: User B không thể xem Task của User A")
    void getTaskById_UserCannotAccessOtherUsersTask() {
        when(taskRepository.findByIdAndUserId(taskId, userBId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(userBId, taskId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");
    }

    @Test
    @DisplayName("UpdateTask - BR-TASK-002: Chặn chuyển sang IN_PROGRESS khi user đã có 10 task IN_PROGRESS")
    void updateTask_WipLimitExceeded_ThrowsBusinessRuleViolationException() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findByIdAndUserId(taskId, userAId)).thenReturn(Optional.of(task));
        when(taskRepository.countByUserIdAndStatus(userAId, TaskStatus.IN_PROGRESS)).thenReturn(10L);

        assertThatThrownBy(() -> taskService.updateTask(userAId, taskId, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("WIP limit exceeded");

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("UpdateTask - Cập nhật trạng thái sang COMPLETED set completedAt")
    void updateTask_CompleteTask_SetsCompletedAt() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.COMPLETED);

        when(taskRepository.findByIdAndUserId(taskId, userAId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskMapper.toTaskResponse(any(Task.class))).thenReturn(taskResponse);

        taskService.updateTask(userAId, taskId, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("UpdateTask - Reopen task từ COMPLETED về TODO xóa completedAt")
    void updateTask_ReopenTask_ClearsCompletedAt() {
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(Instant.now());

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.TODO);

        when(taskRepository.findByIdAndUserId(taskId, userAId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskMapper.toTaskResponse(any(Task.class))).thenReturn(taskResponse);

        taskService.updateTask(userAId, taskId, request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(task.getCompletedAt()).isNull();
    }
}