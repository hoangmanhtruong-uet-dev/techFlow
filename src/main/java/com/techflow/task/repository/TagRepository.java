package com.techflow.task.repository;

import com.techflow.task.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByUserId(UUID userId);

    Optional<Tag> findByIdAndUserId(UUID id, UUID userId);

    Optional<Tag> findByNameIgnoreCaseAndUserId(String name, UUID userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, UUID userId, UUID id);

    boolean existsByNormalizedNameAndUserId(String normalizedName, UUID userId);

    boolean existsByNormalizedNameAndUserIdAndIdNot(String normalizedName, UUID userId, UUID id);

    List<Tag> findByIdInAndUserId(List<UUID> ids, UUID userId);
}