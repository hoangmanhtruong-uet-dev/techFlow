package com.techflow.task.controller;

import com.techflow.common.response.ApiResponse;
import com.techflow.security.util.SecurityUtils;
import com.techflow.task.dto.CreateSubtaskRequest;
import com.techflow.task.dto.SubtaskResponse;
import com.techflow.task.dto.UpdateSubtaskRequest;
import com.techflow.task.service.SubtaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    @PostMapping
    public ResponseEntity<ApiResponse<SubtaskResponse>> createSubtask(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateSubtaskRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        SubtaskResponse response = subtaskService.createSubtask(userId, taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{subtaskId}")
    public ResponseEntity<ApiResponse<SubtaskResponse>> updateSubtask(
            @PathVariable UUID taskId,
            @PathVariable UUID subtaskId,
            @Valid @RequestBody UpdateSubtaskRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        SubtaskResponse response = subtaskService.updateSubtask(userId, taskId, subtaskId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<Void> deleteSubtask(
            @PathVariable UUID taskId,
            @PathVariable UUID subtaskId) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        subtaskService.deleteSubtask(userId, taskId, subtaskId);
        return ResponseEntity.noContent().build();
    }
}