package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.Entities.Stories.StoryView;

public interface StoryViewService {
    Long countByStoryId(Long storyId);
    Boolean existsByStoryId(Long storyId);
    StoryView addView(Long storyId );
}