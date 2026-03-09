package com.start.demo.Services.Community.Stories.Interaction;

import com.start.demo.Entities.Stories.StoryLike;

public interface StoryLikesService {
    Long countByStoryId(Long storyId);
    Boolean existsByStoryId(Long storyId);
    StoryLike addLike(Long storyId);
    String deleteByStoryId(Long storyId);
}