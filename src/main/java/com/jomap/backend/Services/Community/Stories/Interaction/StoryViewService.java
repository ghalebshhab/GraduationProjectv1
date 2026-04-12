package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;

public interface StoryViewService {
    ApiResponse<Long> countByStoryId(Long storyId);
    ApiResponse<Boolean> existsByStoryId(Long storyId);
    ApiResponse<String> addView(Long storyId);
}