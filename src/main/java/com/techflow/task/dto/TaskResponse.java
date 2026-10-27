package com.techflow.task.dto;

import com.techflow.project.dto.ProjectResponse;
import com.techflow.reminder.dto.ReminderResponse;
import com.techflow.task.model.RecurrenceType;
import com.techflow.task.model.TaskPriority;
import com.techflow.task.model.TaskStatus;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Instant deadline;
    private Instant completedAt;
    private RecurrenceType recurrenceType;
    private UUID recurrenceParentId;
    private ProjectResponse project;

    @Builder.Default
    private List<TagResponse> tags = new ArrayList<>();

    @Builder.Default
    private List<SubtaskResponse> subtasks = new ArrayList<>();

    @Builder.Default
    private List<ReminderResponse> reminders = new ArrayList<>();

    private Instant createdAt;
    private Instant updatedAt;
}