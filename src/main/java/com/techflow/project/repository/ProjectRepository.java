package com.techflow.project.repository;

import com.techflow.project.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    Page<Project> findByUserId(UUID userId, Pageable pageable);

    Optional<Project> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByNameIgnoreCaseAndUserId(String name, UUID userId);

    boolean existsByNameIgnoreCaseAndUserIdAndIdNot(String name, UUID userId, UUID id);
}