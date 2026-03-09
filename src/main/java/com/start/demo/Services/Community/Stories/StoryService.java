package com.start.demo.Services.Community.Stories;

import com.start.demo.DTOs.Stories.CreateStoryRequest;
import com.start.demo.DTOs.Stories.StoryResponse;

import java.util.List;

public interface StoryService {

    StoryResponse create(CreateStoryRequest request);

    List<StoryResponse> getActiveStories(int page, int size);

    List<StoryResponse> getActiveStoriesByUser(Long userId, int page, int size);

    String deleteStory(Long storyId);
}