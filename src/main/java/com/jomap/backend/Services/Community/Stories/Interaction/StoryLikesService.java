package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.Entities.Stories.StoryLike;

public interface StoryLikesService {
    Long countByStoryId(Long storyId);
    Boolean existsByStoryId(Long storyId);
    StoryLike addLike(Long storyId);
    String deleteByStoryId(Long storyId);
}