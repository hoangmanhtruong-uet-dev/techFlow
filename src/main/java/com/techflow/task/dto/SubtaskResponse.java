package com.techflow.task.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskResponse {

    private UUID id;
    private UUID taskId;
    private String title;
    private boolean completed;
    private Instant completedAt;
    private Integer position;
}