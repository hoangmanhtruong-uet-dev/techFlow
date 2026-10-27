package com.techflow.project.controller;

import com.techflow.common.response.ApiResponse;
import com.techflow.project.dto.CreateProjectRequest;
import com.techflow.project.dto.ProjectResponse;
import com.techflow.project.dto.UpdateProjectRequest;
import com.techflow.project.service.ProjectService;
import com.techflow.security.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @PageableDefault(size = 20) Pageable pageable) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        ApiResponse<List<ProjectResponse>> response = projectService.getProjects(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        ProjectResponse response = projectService.createProject(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID id) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        ProjectResponse response = projectService.getProjectById(userId, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        ProjectResponse response = projectService.updateProject(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        projectService.deleteProject(userId, id);
        return ResponseEntity.noContent().build();
    }
}