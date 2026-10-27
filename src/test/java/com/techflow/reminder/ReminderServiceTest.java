package com.techflow.reminder;

import com.techflow.common.exception.BusinessRuleViolationException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.reminder.dto.CreateReminderRequest;
import com.techflow.reminder.dto.ReminderResponse;
import com.techflow.reminder.model.Reminder;
import com.techflow.reminder.model.ReminderStatus;
import com.techflow.reminder.repository.ReminderRepository;
import com.techflow.reminder.service.ReminderService;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.Task;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.repository.TaskRepository;
import com.techflow.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private ReminderService reminderService;

    private UUID userId;
    private UUID taskId;
    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        user = User.builder().id(userId).email("user@example.com").build();

        task = Task.builder()
                .id(taskId)
                .user(user)
                .title("Task with Reminder")
                .status(TaskStatus.TODO)
                .deadline(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();
    }

    @Test
    @DisplayName("CreateReminder - Thành công khi remind_at hợp lệ")
    void createReminder_Success() {
        CreateReminderRequest request = new CreateReminderRequest();
        request.setRemindAt(Instant.now().plus(1, ChronoUnit.DAYS));

        Reminder reminder = Reminder.builder()
                .id(UUID.randomUUID())
                .task(task)
                .remindAt(request.getRemindAt())
                .status(ReminderStatus.PENDING)
                .build();

        ReminderResponse response = ReminderResponse.builder()
                .id(reminder.getId())
                .remindAt(reminder.getRemindAt())
                .status(ReminderStatus.PENDING)
                .build();

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);
        when(taskMapper.toReminderResponse(reminder)).thenReturn(response);

        ReminderResponse result = reminderService.createReminder(userId, taskId, request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ReminderStatus.PENDING);
        verify(reminderRepository).save(any(Reminder.class));
    }

    @Test
    @DisplayName("CreateReminder - BR-REMINDER-001: Chặn tạo reminder cho Task đã COMPLETED")
    void createReminder_TaskCompleted_ThrowsBusinessRuleViolationException() {
        task.setStatus(TaskStatus.COMPLETED);

        CreateReminderRequest request = new CreateReminderRequest();
        request.setRemindAt(Instant.now().plus(1, ChronoUnit.HOURS));

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> reminderService.createReminder(userId, taskId, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Cannot create reminder for completed or cancelled task");

        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("CreateReminder - BR-REMINDER-001: Chặn khi remind_at sau deadline của Task")
    void createReminder_RemindAtAfterDeadline_ThrowsBusinessRuleViolationException() {
        task.setDeadline(Instant.now().plus(1, ChronoUnit.HOURS));

        CreateReminderRequest request = new CreateReminderRequest();
        request.setRemindAt(Instant.now().plus(2, ChronoUnit.HOURS));

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> reminderService.createReminder(userId, taskId, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("remind_at must be before task deadline");

        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("CreateReminder - BR-REMINDER-001: Chặn khi remind_at trong quá khứ")
    void createReminder_RemindAtInPast_ThrowsBusinessRuleViolationException() {
        CreateReminderRequest request = new CreateReminderRequest();
        request.setRemindAt(Instant.now().minus(1, ChronoUnit.HOURS));

        when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> reminderService.createReminder(userId, taskId, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("remind_at must be in the future");

        verify(reminderRepository, never()).save(any(Reminder.class));
    }
}