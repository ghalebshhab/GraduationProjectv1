package com.jomap.backend.Services.Community.Stories;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.CreateStoryRequest;
import com.jomap.backend.DTOs.Stories.StoryResponse;

import java.util.List;

public interface StoryService {

    ApiResponse<StoryResponse> create(CreateStoryRequest request);

    ApiResponse<List<StoryResponse>> getActiveStories(int page, int size);

    ApiResponse<List<StoryResponse>> getActiveStoriesByUser(Long userId, int page, int size);

    ApiResponse<String> deleteStory(Long storyId);
}