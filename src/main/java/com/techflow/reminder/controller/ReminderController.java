package com.techflow.reminder.controller;

import com.techflow.common.response.ApiResponse;
import com.techflow.reminder.dto.CreateReminderRequest;
import com.techflow.reminder.dto.ReminderResponse;
import com.techflow.reminder.service.ReminderService;
import com.techflow.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReminderResponse>> createReminder(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateReminderRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        ReminderResponse response = reminderService.createReminder(userId, taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(
            @PathVariable UUID taskId,
            @PathVariable UUID id) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        reminderService.deleteReminder(userId, taskId, id);
        return ResponseEntity.noContent().build();
    }
}