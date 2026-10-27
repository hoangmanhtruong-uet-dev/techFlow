package com.techflow.task.repository;

import com.techflow.task.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {

    List<Subtask> findByTaskIdOrderByPositionAsc(UUID taskId);

    @Query("SELECT s FROM Subtask s WHERE s.id = :id AND s.task.user.id = :userId")
    Optional<Subtask> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    Optional<Subtask> findByIdAndTaskId(UUID id, UUID taskId);

    @Query("SELECT s FROM Subtask s WHERE s.id = :id AND s.task.id = :taskId AND s.task.user.id = :userId")
    Optional<Subtask> findByIdAndTaskIdAndUserId(@Param("id") UUID id, @Param("taskId") UUID taskId, @Param("userId") UUID userId);

    @Query("SELECT COALESCE(MAX(s.position), 0) FROM Subtask s WHERE s.task.id = :taskId")
    int findMaxPositionByTaskId(@Param("taskId") UUID taskId);

    long countByTaskId(UUID taskId);

    long countByTaskIdAndCompletedTrue(UUID taskId);
}