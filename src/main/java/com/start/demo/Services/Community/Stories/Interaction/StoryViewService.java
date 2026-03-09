package com.start.demo.Services.Community.Stories.Interaction;

import com.start.demo.Entities.Stories.StoryView;

public interface StoryViewService {
    Long countByStoryId(Long storyId);
    Boolean existsByStoryId(Long storyId);
    StoryView addView(Long storyId );
}