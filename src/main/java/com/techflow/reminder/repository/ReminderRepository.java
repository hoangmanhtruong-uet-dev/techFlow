package com.techflow.reminder.repository;

import com.techflow.reminder.model.Reminder;
import com.techflow.reminder.model.ReminderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    @Query("SELECT r FROM Reminder r WHERE r.task.user.id = :userId")
    Page<Reminder> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT r FROM Reminder r WHERE r.id = :id AND r.task.user.id = :userId")
    Optional<Reminder> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query("SELECT r FROM Reminder r WHERE r.task.id = :taskId AND r.task.user.id = :userId")
    List<Reminder> findByTaskIdAndUserId(@Param("taskId") UUID taskId, @Param("userId") UUID userId);

    Optional<Reminder> findByIdAndTaskId(UUID id, UUID taskId);

    List<Reminder> findByStatusAndRemindAtLessThanEqual(ReminderStatus status, Instant now);
}