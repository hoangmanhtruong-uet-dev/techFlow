package com.techflow.task.dto;

import com.techflow.task.model.RecurrenceType;
import com.techflow.task.model.TaskPriority;
import com.techflow.task.model.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    private UUID projectId;

    @Size(max = 255, message = "title cannot exceed 255 characters")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Instant deadline;

    private RecurrenceType recurrenceType;

    private List<UUID> tagIds;
}