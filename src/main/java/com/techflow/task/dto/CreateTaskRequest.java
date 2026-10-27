package com.techflow.task.dto;

import com.techflow.task.model.RecurrenceType;
import com.techflow.task.model.TaskPriority;
import jakarta.validation.constraints.NotBlank;
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
public class CreateTaskRequest {

    private UUID projectId;

    @NotBlank(message = "title cannot be blank")
    @Size(max = 255, message = "title cannot exceed 255 characters")
    private String title;

    private String description;

    private TaskPriority priority;

    private Instant deadline;

    private RecurrenceType recurrenceType;

    private List<UUID> tagIds;
}