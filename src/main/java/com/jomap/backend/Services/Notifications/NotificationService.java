package com.jomap.backend.Services.Notifications;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Notifications.NotificationRequest;
import com.jomap.backend.DTOs.Notifications.NotificationResponse;

import java.util.List;

public interface NotificationService {

    ApiResponse<NotificationResponse> sendNotification(NotificationRequest request);

    ApiResponse<List<NotificationResponse>> getUserNotifications(String email);

    ApiResponse<List<NotificationResponse>> getUserNotificationsByCategory(String email, String category);

    ApiResponse<Long> getUnreadCount(String email);

    ApiResponse<NotificationResponse> markAsRead(Long notificationId, String email);

    ApiResponse<String> markAllAsRead(String email);
}
