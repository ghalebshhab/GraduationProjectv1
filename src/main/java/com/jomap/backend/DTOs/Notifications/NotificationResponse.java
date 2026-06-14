package com.jomap.backend.DTOs.Notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    
    private Long id;
    private String text;
    private String type;
    private String category;
    
    private Long toUserId;
    private Long fromUserId;
    private String fromUsername;
    private String fromUserProfileImage;
    
    private Long activityId;
    private Long postId;
    private Long offerId;
    private Long locationId;
    
    private Boolean isRead;
    private String createdAt;
}
