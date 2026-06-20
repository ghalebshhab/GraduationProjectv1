package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.Views.StoryViewerResponse;
import java.util.List;

public interface StoryViewService {
    ApiResponse<Long> countByStoryId(Long storyId);
    ApiResponse<Boolean> existsByStoryId(Long storyId);
    ApiResponse<String> addView(Long storyId);
    ApiResponse<List<StoryViewerResponse>> getViewersByStoryId(Long storyId);
}