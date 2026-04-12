package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;

public interface StoryLikesService {
    ApiResponse<Long> countByStoryId(Long storyId);
    ApiResponse<Boolean> existsByStoryId(Long storyId);
    ApiResponse<String> addLike(Long storyId);
    ApiResponse<String> deleteByStoryId(Long storyId);
}