package com.jomap.backend.DTOs.Notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private String text;
    private String type;
    private String category;
    
    private Long toUserId;
    private Long fromUserId; // Nullable for system notifications
    
    private Long activityId; // Nullable
    private Long postId; // Nullable
    private Long offerId; // Nullable
    private Long locationId; // Nullable
}
