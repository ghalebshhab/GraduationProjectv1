package com.jomap.backend.Services.Community.Stories;

import com.jomap.backend.DTOs.Stories.CreateStoryRequest;
import com.jomap.backend.DTOs.Stories.StoryResponse;

import java.util.List;

public interface StoryService {

    StoryResponse create(CreateStoryRequest request);

    List<StoryResponse> getActiveStories(int page, int size);

    List<StoryResponse> getActiveStoriesByUser(Long userId, int page, int size);

    String deleteStory(Long storyId);
}