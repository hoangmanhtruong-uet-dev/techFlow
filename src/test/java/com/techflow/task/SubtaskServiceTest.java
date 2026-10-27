package com.techflow.task;

import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.task.dto.CreateSubtaskRequest;
import com.techflow.task.dto.SubtaskResponse;
import com.techflow.task.dto.UpdateSubtaskRequest;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.Subtask;
import com.techflow.task.model.Task;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.repository.SubtaskRepository;
import com.techflow.task.repository.TaskRepository;
import com.techflow.task.service.SubtaskService;
import com.techflow.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubtaskServiceTest {

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private SubtaskService subtaskService;

    private UUID userId;
    private UUID otherUserId;
    private UUID taskId;
    private UUID subtaskId;
    private User user;
    private Task task;
    private Subtask subtask1;
    private Subtask subtask2;
    private SubtaskResponse subtaskResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        subtaskId = UUID.randomUUID();

        user = User.builder().id(userId).email("user@example.com").build();

        task = Task.builder()
                .id(taskId)
                .user(user)
                .title("Parent Task")
                .status(TaskStatus.TODO)
                .subtasks(new ArrayList<>())
                .build();

        subtask1 = Subtask.builder()
                .id(subtaskId)
                .task(task)
                .title("Subtask 1")
                .completed(false)
                .position(0)
                .build();

        subtask2 = Subtask.builder()
                .id(UUID.randomUUID())
                .task(task)
                .title("Subtask 2")
                .completed(false)
                .position(1)
                .build();

        subtaskResponse = SubtaskResponse.builder()
                .id(subtaskId)
                .title("Subtask 1")
                .completed(true)
                .build();
    }

    @Test
    @DisplayName("CreateSubtask - BR-AUTH-001: Không cho tạo subtask trên Task của user khác")
    void createSubtask_OtherUsersTask_ThrowsResourceNotFoundException() {
        CreateSubtaskRequest request = new CreateSubtaskRequest();
        request.setTitle("Subtask Title");

        when(taskRepository.findByIdAndUserId(taskId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subtaskService.createSubtask(otherUserId, taskId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found");

        verify(subtaskRepository, never()).save(any(Subtask.class));
    }

    @Test
    @DisplayName("BR-TASK-001: Task tự động chuyển sang IN_PROGRESS khi có ít nhất 1 subtask hoàn thành")
    void updateSubtask_OneSubtaskCompleted_TaskBecomesInProgress() {
        subtask1.setCompleted(false);
        subtask2.setCompleted(false);
        task.setStatus(TaskStatus.TODO);

        UpdateSubtaskRequest request = new UpdateSubtaskRequest();
        request.setIsCompleted(true);

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByIdAndTaskId(subtaskId, taskId)).thenReturn(Optional.of(subtask1));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(subtaskRepository.findByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(subtask1, subtask2));
        when(taskMapper.toSubtaskResponse(any(Subtask.class))).thenReturn(subtaskResponse);

        SubtaskResponse response = subtaskService.updateSubtask(userId, taskId, subtaskId, request);

        assertThat(response).isNotNull();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getCompletedAt()).isNull();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("BR-TASK-001: Task tự động chuyển sang COMPLETED khi TẤT CẢ subtasks hoàn thành")
    void updateSubtask_AllSubtasksCompleted_TaskBecomesCompleted() {
        subtask1.setCompleted(true);
        subtask2.setCompleted(false);
        task.setStatus(TaskStatus.IN_PROGRESS);

        UpdateSubtaskRequest request = new UpdateSubtaskRequest();
        request.setIsCompleted(true);

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByIdAndTaskId(subtask2.getId(), taskId)).thenReturn(Optional.of(subtask2));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(inv -> inv.getArgument(0));

        // When subtask2 is saved as completed
        when(subtaskRepository.findByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(subtask1, subtask2));
        when(taskMapper.toSubtaskResponse(any(Subtask.class))).thenReturn(subtaskResponse);

        subtaskService.updateSubtask(userId, taskId, subtask2.getId(), request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(task.getCompletedAt()).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("BR-TASK-001: Task chuyển từ COMPLETED về IN_PROGRESS khi bỏ hoàn thành 1 subtask")
    void updateSubtask_UncheckSubtaskFromCompleted_TaskRevertsToInProgress() {
        subtask1.setCompleted(true);
        subtask2.setCompleted(true);
        task.setStatus(TaskStatus.COMPLETED);

        UpdateSubtaskRequest request = new UpdateSubtaskRequest();
        request.setIsCompleted(false);

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(subtaskRepository.findByIdAndTaskId(subtask2.getId(), taskId)).thenReturn(Optional.of(subtask2));
        when(subtaskRepository.save(any(Subtask.class))).thenAnswer(inv -> inv.getArgument(0));

        // When subtask2 is unchecked
        when(subtaskRepository.findByTaskIdOrderByPositionAsc(taskId)).thenReturn(List.of(subtask1, subtask2));
        when(taskMapper.toSubtaskResponse(any(Subtask.class))).thenReturn(subtaskResponse);

        subtaskService.updateSubtask(userId, taskId, subtask2.getId(), request);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getCompletedAt()).isNull();
        verify(taskRepository).save(task);
    }
}