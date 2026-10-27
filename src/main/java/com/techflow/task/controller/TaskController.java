package com.techflow.task.controller;

import com.techflow.common.response.ApiResponse;
import com.techflow.common.response.PageMeta;
import com.techflow.security.util.SecurityUtils;
import com.techflow.task.dto.CreateTaskRequest;
import com.techflow.task.dto.TaskResponse;
import com.techflow.task.dto.UpdateTaskRequest;
import com.techflow.task.model.TaskPriority;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant deadlineTo,
            @PageableDefault(size = 20) Pageable pageable) {

        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        Page<TaskResponse> page = taskService.getTasks(userId, projectId, status, priority, tag, search, deadlineFrom, deadlineTo, pageable);

        PageMeta meta = PageMeta.builder()
                .page(page.getNumber())
                .limit(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success(page.getContent(), meta));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        TaskResponse response = taskService.createTask(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable UUID id) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        TaskResponse response = taskService.getTaskById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        TaskResponse response = taskService.updateTask(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        taskService.deleteTask(userId, id);
        return ResponseEntity.noContent().build();
    }
}