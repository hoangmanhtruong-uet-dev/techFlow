package com.techflow.task.service;

import com.techflow.common.exception.BusinessRuleViolationException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.task.dto.CreateSubtaskRequest;
import com.techflow.task.dto.SubtaskResponse;
import com.techflow.task.dto.UpdateSubtaskRequest;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.Subtask;
import com.techflow.task.model.Task;
import com.techflow.task.model.TaskStatus;
import com.techflow.task.repository.SubtaskRepository;
import com.techflow.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubtaskService {

    private static final int MAX_SUBTASKS_PER_TASK = 20;
    private static final int MAX_WIP_LIMIT = 10;

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public SubtaskResponse createSubtask(UUID userId, UUID taskId, CreateSubtaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // BR-TASK-001: Tối đa 20 subtasks trên 1 task
        long currentCount = subtaskRepository.countByTaskId(taskId);
        if (currentCount >= MAX_SUBTASKS_PER_TASK) {
            throw new BusinessRuleViolationException("MAX_SUBTASKS_EXCEEDED", "Task cannot have more than " + MAX_SUBTASKS_PER_TASK + " subtasks");
        }

        Subtask subtask = Subtask.builder()
                .task(task)
                .title(request.getTitle().trim())
                .completed(false)
                .position((int) currentCount)
                .build();

        Subtask saved = subtaskRepository.save(subtask);
        log.info("Created subtask {} for task {}", saved.getId(), taskId);

        // Update task status based on subtasks
        recalculateTaskStatus(userId, task);

        return taskMapper.toSubtaskResponse(saved);
    }

    @Transactional
    public SubtaskResponse updateSubtask(UUID userId, UUID taskId, UUID subtaskId, UpdateSubtaskRequest request) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Subtask subtask = subtaskRepository.findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with id: " + subtaskId));

        if (request.getTitle() != null) {
            subtask.setTitle(request.getTitle().trim());
        }

        if (request.getIsCompleted() != null) {
            subtask.setCompleted(request.getIsCompleted());
            if (request.getIsCompleted()) {
                subtask.setCompletedAt(java.time.Instant.now());
            } else {
                subtask.setCompletedAt(null);
            }
        }

        Subtask saved = subtaskRepository.save(subtask);

        // Recalculate parent task status
        recalculateTaskStatus(userId, task);

        return taskMapper.toSubtaskResponse(saved);
    }

    @Transactional
    public void deleteSubtask(UUID userId, UUID taskId, UUID subtaskId) {
        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Subtask subtask = subtaskRepository.findByIdAndTaskId(subtaskId, taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with id: " + subtaskId));

        subtaskRepository.delete(subtask);
        log.info("Deleted subtask {} from task {}", subtaskId, taskId);

        // Recalculate parent task status after deletion
        recalculateTaskStatus(userId, task);
    }

    /**
     * BR-TASK-001: Tự động tính trạng thái Task từ Subtasks:
     * - 100% subtasks COMPLETED -> Task chuyển sang COMPLETED
     * - >= 1 subtask COMPLETED và < 100% -> Task chuyển sang IN_PROGRESS
     * - 0% subtask COMPLETED -> Giữ nguyên trạng thái hiện tại (hoặc TODO nếu chưa bắt đầu)
     *
     * BR-TASK-002: Giới hạn WIP:
     * - Trước khi chuyển một Task sang trạng thái IN_PROGRESS (hoặc tự động chuyển do Subtask),
     *   kiểm tra tổng số Task IN_PROGRESS hiện tại của user. Nếu đã đạt trần (10 tasks), ném BusinessRuleViolationException
     */
    public void recalculateTaskStatus(UUID userId, Task task) {
        List<Subtask> subtasks = subtaskRepository.findByTaskIdOrderByPositionAsc(task.getId());
        if (subtasks.isEmpty()) {
            return;
        }

        long total = subtasks.size();
        long completedCount = subtasks.stream().filter(Subtask::isCompleted).count();

        TaskStatus newStatus;
        if (completedCount == total) {
            newStatus = TaskStatus.COMPLETED;
        } else if (completedCount > 0) {
            newStatus = TaskStatus.IN_PROGRESS;
        } else {
            // 0% completed -> Nếu task đang là COMPLETED thì lùi về IN_PROGRESS hoặc TODO
            if (task.getStatus() == TaskStatus.COMPLETED) {
                newStatus = TaskStatus.TODO;
            } else {
                return; // Giữ nguyên trạng thái hiện tại
            }
        }

        if (newStatus != task.getStatus()) {
            if (newStatus == TaskStatus.IN_PROGRESS) {
                checkWipLimit(userId);
                task.setCompletedAt(null);
            } else if (newStatus == TaskStatus.COMPLETED) {
                task.setCompletedAt(Instant.now());
            } else if (newStatus == TaskStatus.TODO) {
                task.setCompletedAt(null);
            }
            task.setStatus(newStatus);
            taskRepository.save(task);
            log.info("Auto-updated task {} status to {} due to subtask completion ({}/{})",
                    task.getId(), newStatus, completedCount, total);
        }
    }

    private void checkWipLimit(UUID userId) {
        long inProgressCount = taskRepository.countByUserIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        if (inProgressCount >= MAX_WIP_LIMIT) {
            throw new BusinessRuleViolationException("WIP_LIMIT_EXCEEDED",
                    "WIP limit exceeded: user already has " + inProgressCount + " tasks IN_PROGRESS (max: " + MAX_WIP_LIMIT + ")");
        }
    }
}