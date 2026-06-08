package com.jomap.backend.DTOs.Feedback;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userProfileImageUrl;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    
    private String ownerReply;
    private LocalDateTime repliedAt;
}
