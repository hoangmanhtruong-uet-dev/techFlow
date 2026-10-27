package com.techflow.task.mapper;

import com.techflow.project.mapper.ProjectMapper;
import com.techflow.reminder.dto.ReminderResponse;
import com.techflow.reminder.model.Reminder;
import com.techflow.task.dto.SubtaskResponse;
import com.techflow.task.dto.TagResponse;
import com.techflow.task.dto.TaskResponse;
import com.techflow.task.model.Subtask;
import com.techflow.task.model.Tag;
import com.techflow.task.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskMapper {

    private final ProjectMapper projectMapper;

    public TagResponse toTagResponse(Tag tag) {
        if (tag == null) return null;
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }

    public SubtaskResponse toSubtaskResponse(Subtask subtask) {
        if (subtask == null) return null;
        return SubtaskResponse.builder()
                .id(subtask.getId())
                .taskId(subtask.getTask() != null ? subtask.getTask().getId() : null)
                .title(subtask.getTitle())
                .completed(subtask.isCompleted())
                .completedAt(subtask.getCompletedAt())
                .position(subtask.getPosition())
                .build();
    }

    public ReminderResponse toReminderResponse(Reminder reminder) {
        if (reminder == null) return null;
        return ReminderResponse.builder()
                .id(reminder.getId())
                .taskId(reminder.getTask() != null ? reminder.getTask().getId() : null)
                .remindAt(reminder.getRemindAt())
                .status(reminder.getStatus())
                .sentAt(reminder.getSentAt())
                .createdAt(reminder.getCreatedAt())
                .build();
    }

    public TaskResponse toTaskResponse(Task task) {
        if (task == null) return null;
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .deadline(task.getDeadline())
                .completedAt(task.getCompletedAt())
                .recurrenceType(task.getRecurrenceType())
                .recurrenceParentId(task.getRecurrenceParentId())
                .project(projectMapper.toResponse(task.getProject()))
                .tags(task.getTags() != null
                        ? task.getTags().stream().map(this::toTagResponse).collect(Collectors.toList())
                        : Collections.emptyList())
                .subtasks(task.getSubtasks() != null
                        ? task.getSubtasks().stream().map(this::toSubtaskResponse).collect(Collectors.toList())
                        : Collections.emptyList())
                .reminders(task.getReminders() != null
                        ? task.getReminders().stream().map(this::toReminderResponse).collect(Collectors.toList())
                        : Collections.emptyList())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}