package com.jomap.backend.DTOs.Feedback;

import java.time.LocalDateTime;

import com.jomap.backend.Entities.Feedback.TargetType;

import lombok.Data;

@Data
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userProfileImageUrl;
    
    private Long targetId;
    private TargetType targetType;
    private String targetName;
    private String targetImageUrl;

    private Integer rating;
    private String comment;
    
    private Boolean isEdited;
    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;
    
    private String ownerReply;
    private LocalDateTime repliedAt;
    
    private Boolean isReplyEdited;
    private LocalDateTime replyUpdatedAt;
}
