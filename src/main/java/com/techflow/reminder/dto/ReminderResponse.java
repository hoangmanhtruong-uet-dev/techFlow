package com.techflow.reminder.dto;

import com.techflow.reminder.model.ReminderStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderResponse {

    private UUID id;
    private UUID taskId;
    private Instant remindAt;
    private ReminderStatus status;
    private Instant sentAt;
    private Instant createdAt;
}