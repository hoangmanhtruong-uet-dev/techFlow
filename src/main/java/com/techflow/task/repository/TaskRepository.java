package com.techflow.task.repository;

import com.techflow.task.model.Task;
import com.techflow.task.model.TaskPriority;
import com.techflow.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:projectId IS NULL OR t.project.id = :projectId) " +
           "AND (:tag IS NULL OR EXISTS (SELECT tg FROM t.tags tg WHERE LOWER(tg.name) = LOWER(:tag))) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR (t.description IS NOT NULL AND LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))) " +
           "AND (:deadlineFrom IS NULL OR t.deadline >= :deadlineFrom) " +
           "AND (:deadlineTo IS NULL OR t.deadline <= :deadlineTo)")
    Page<Task> searchTasks(@Param("userId") UUID userId,
                           @Param("status") TaskStatus status,
                           @Param("priority") TaskPriority priority,
                           @Param("projectId") UUID projectId,
                           @Param("tag") String tag,
                           @Param("search") String search,
                           @Param("deadlineFrom") Instant deadlineFrom,
                           @Param("deadlineTo") Instant deadlineTo,
                           Pageable pageable);

    List<Task> findByProjectId(UUID projectId);
}