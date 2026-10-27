package com.techflow.reminder.service;

import com.techflow.common.exception.BusinessRuleViolationException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.reminder.dto.CreateReminderRequest;
import com.techflow.reminder.dto.ReminderResponse;
import com.techflow.reminder.model.Reminder;
import com.techflow.reminder.model.ReminderStatus;
import com.techflow.reminder.repository.ReminderRepository;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.Task;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public ReminderResponse createReminder(UUID userId, UUID taskId, CreateReminderRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // BR-REMINDER-001: Không thể tạo reminder cho task đã hoàn thành hoặc đã hủy
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new BusinessRuleViolationException("TASK_ALREADY_COMPLETED", "Cannot create reminder for completed or cancelled task");
        }

        // BR-REMINDER-001: remind_at phải trước deadline của task (nếu task có deadline)
        if (task.getDeadline() != null && !request.getRemindAt().isBefore(task.getDeadline())) {
            throw new BusinessRuleViolationException("REMINDER_AFTER_DEADLINE", "remind_at must be before task deadline (" + task.getDeadline() + ")");
        }

        // BR-REMINDER-001: remind_at phải ở tương lai
        if (!request.getRemindAt().isAfter(Instant.now())) {
            throw new BusinessRuleViolationException("REMINDER_IN_PAST", "remind_at must be in the future");
        }

        Reminder reminder = Reminder.builder()
                .task(task)
                .remindAt(request.getRemindAt())
                .status(ReminderStatus.PENDING)
                .build();

        Reminder saved = reminderRepository.save(reminder);
        log.info("Created reminder {} for task {}", saved.getId(), taskId);
        return taskMapper.toReminderResponse(saved);
    }

    @Transactional
    public void deleteReminder(UUID userId, UUID taskId, UUID reminderId) {
        // Ownership check: task must belong to user
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Reminder reminder = reminderRepository.findByIdAndTaskId(reminderId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with id: " + reminderId));

        reminderRepository.delete(reminder);
        log.info("Deleted reminder {} from task {}", reminderId, taskId);
    }
}