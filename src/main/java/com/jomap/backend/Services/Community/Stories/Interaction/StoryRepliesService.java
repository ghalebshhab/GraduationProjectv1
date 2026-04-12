package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.Replies.StoryReplyResponse;

import java.util.List;

public interface StoryRepliesService {
    ApiResponse<Long> countByStoryId(Long storyId);
    ApiResponse<List<StoryReplyResponse>> findByStoryId(Long storyId);
    ApiResponse<StoryReplyResponse> addReply(Long storyId, String content);
    ApiResponse<StoryReplyResponse> updateReply(Long replyId, String content);
    ApiResponse<String> deleteReply(Long replyId);
}