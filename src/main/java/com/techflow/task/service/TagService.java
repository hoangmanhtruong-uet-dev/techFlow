package com.techflow.task.service;

import com.techflow.common.exception.ConflictException;
import com.techflow.common.exception.ResourceNotFoundException;
import com.techflow.task.dto.CreateTagRequest;
import com.techflow.task.dto.TagResponse;
import com.techflow.task.dto.UpdateTagRequest;
import com.techflow.task.mapper.TaskMapper;
import com.techflow.task.model.Tag;
import com.techflow.task.repository.TagRepository;
import com.techflow.user.model.User;
import com.techflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TagResponse createTag(UUID userId, CreateTagRequest request) {
        String normalizedName = request.getName().trim().toLowerCase();
        if (tagRepository.existsByNormalizedNameAndUserId(normalizedName, userId)) {
            throw new ConflictException("TAG_NAME_DUPLICATE", "Tag with name '" + request.getName() + "' already exists");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Tag tag = Tag.builder()
                .name(request.getName().trim())
                .normalizedName(normalizedName)
                .user(user)
                .build();

        Tag saved = tagRepository.save(tag);
        return taskMapper.toTagResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTags(UUID userId) {
        return tagRepository.findByUserId(userId).stream()
                .map(taskMapper::toTagResponse)
                .toList();
    }

    @Transactional
    public TagResponse updateTag(UUID userId, UUID tagId, UpdateTagRequest request) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        String normalizedName = request.getName().trim().toLowerCase();
        if (!normalizedName.equalsIgnoreCase(tag.getNormalizedName())) {
            if (tagRepository.existsByNormalizedNameAndUserIdAndIdNot(normalizedName, userId, tagId)) {
                throw new ConflictException("TAG_NAME_DUPLICATE", "Tag with name '" + request.getName() + "' already exists");
            }
            tag.setName(request.getName().trim());
            tag.setNormalizedName(normalizedName);
        }

        Tag updated = tagRepository.save(tag);
        return taskMapper.toTagResponse(updated);
    }

    @Transactional
    public void deleteTag(UUID userId, UUID tagId) {
        Tag tag = tagRepository.findByIdAndUserId(tagId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        tagRepository.delete(tag);
        log.info("Tag {} deleted for user {}", tagId, userId);
    }
}