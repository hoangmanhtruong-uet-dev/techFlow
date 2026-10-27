package com.techflow.reminder.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReminderRequest {

    @NotNull(message = "remindAt cannot be null")
    @Future(message = "remindAt must be in the future")
    private Instant remindAt;
}