package com.jomap.backend.Services.Community.Stories.Interaction;
import com.jomap.backend.Entities.Stories.StoryReply;

import java.util.List;

public interface StoryRepliesService {
    Long countByStoryId(Long storyId);
    List<StoryReply> findByStoryId(Long storyId);
    StoryReply addReply(Long storyId, String content);
    StoryReply updateReply(Long replyId, String content);
    String deleteReply(Long replyId);
}