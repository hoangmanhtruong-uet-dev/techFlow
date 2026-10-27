package com.techflow.task.controller;

import com.techflow.common.response.ApiResponse;
import com.techflow.security.util.SecurityUtils;
import com.techflow.task.dto.CreateTagRequest;
import com.techflow.task.dto.TagResponse;
import com.techflow.task.dto.UpdateTagRequest;
import com.techflow.task.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        List<TagResponse> response = tagService.getTags(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        TagResponse response = tagService.createTag(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTagRequest request) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        TagResponse response = tagService.updateTag(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable UUID id) {
        UUID userId = SecurityUtils.getRequiredCurrentUserId();
        tagService.deleteTag(userId, id);
        return ResponseEntity.noContent().build();
    }
}