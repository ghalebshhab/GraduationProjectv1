package com.jomap.backend.Services.Notifications;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Notifications.NotificationRequest;
import com.jomap.backend.DTOs.Notifications.NotificationResponse;
import com.jomap.backend.Entities.Notifications.Notification;
import com.jomap.backend.Entities.Notifications.NotificationCategory;
import com.jomap.backend.Entities.Notifications.NotificationRepository;
import com.jomap.backend.Entities.Notifications.NotificationType;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> sendNotification(NotificationRequest request) {
        Optional<User> toUserOpt = userRepository.findById(request.getToUserId());
        if (toUserOpt.isEmpty()) {
            return ApiResponse.error("المستخدم المستلم غير موجود");
        }

        User fromUser = null;
        if (request.getFromUserId() != null) {
            Optional<User> fromUserOpt = userRepository.findById(request.getFromUserId());
            if (fromUserOpt.isPresent()) {
                fromUser = fromUserOpt.get();
            }
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("نوع الإشعار غير صحيح");
        }

        NotificationCategory category;
        try {
            category = NotificationCategory.valueOf(request.getCategory().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("تصنيف الإشعار غير صحيح");
        }

        Notification notification = Notification.builder()
                .text(request.getText())
                .type(type)
                .category(category)
                .toUser(toUserOpt.get())
                .fromUser(fromUser)
                .activityId(request.getActivityId())
                .postId(request.getPostId())
                .offerId(request.getOfferId())
                .locationId(request.getLocationId())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        return ApiResponse.success("تم إرسال الإشعار بنجاح", mapToResponse(saved));
    }

    @Override
    public ApiResponse<List<NotificationResponse>> getUserNotifications(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        List<NotificationResponse> responses = notificationRepository.findByToUserOrderByCreatedAtDesc(userOpt.get())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("تم جلب الإشعارات بنجاح", responses);
    }

    @Override
    public ApiResponse<List<NotificationResponse>> getUserNotificationsByCategory(String email, String categoryParam) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        NotificationCategory category;
        try {
            category = NotificationCategory.valueOf(categoryParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("تصنيف الإشعار غير صحيح");
        }

        List<NotificationResponse> responses = notificationRepository.findByToUserAndCategoryOrderByCreatedAtDesc(userOpt.get(), category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("تم جلب الإشعارات بنجاح", responses);
    }

    @Override
    public ApiResponse<Long> getUnreadCount(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Long count = notificationRepository.countByToUserAndIsReadFalse(userOpt.get());
        return ApiResponse.success("تم حساب الإشعارات غير المقروءة", count);
    }

    @Override
    @Transactional
    public ApiResponse<NotificationResponse> markAsRead(Long notificationId, String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return ApiResponse.error("الإشعار غير موجود");
        }

        Notification notification = notificationOpt.get();

        if (!notification.getToUser().getId().equals(userOpt.get().getId())) {
            return ApiResponse.error("غير مصرح لك بتعديل هذا الإشعار");
        }

        notification.setIsRead(true);
        Notification saved = notificationRepository.save(notification);

        return ApiResponse.success("تم تحديد الإشعار كمقروء", mapToResponse(saved));
    }

    @Override
    @Transactional
    public ApiResponse<String> markAllAsRead(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        List<Notification> notifications = notificationRepository.findByToUserOrderByCreatedAtDesc(userOpt.get());
        boolean anyChanged = false;
        for (Notification n : notifications) {
            if (Boolean.FALSE.equals(n.getIsRead())) {
                n.setIsRead(true);
                anyChanged = true;
            }
        }

        if (anyChanged) {
            notificationRepository.saveAll(notifications);
        }

        return ApiResponse.success("تم تحديد جميع الإشعارات كمقروءة", null);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        String fromUsername = null;
        String fromUserProfileImage = null;

        if (notification.getFromUser() != null) {
            fromUsername = notification.getFromUser().getUsername();
            if (notification.getFromUser().getProfile() != null && notification.getFromUser().getProfile().getFirstName() != null) {
                fromUsername = notification.getFromUser().getProfile().getFirstName() + " " + notification.getFromUser().getProfile().getLastName();
            }
            if (notification.getFromUser().getProfileImageUrl() != null) {
                fromUserProfileImage = notification.getFromUser().getProfileImageUrl();
            } else if (notification.getFromUser().getProfile() != null && notification.getFromUser().getProfile().getProfileImageUrl() != null) {
                fromUserProfileImage = notification.getFromUser().getProfile().getProfileImageUrl();
            }
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .text(notification.getText())
                .type(notification.getType().name())
                .category(notification.getCategory().name())
                .toUserId(notification.getToUser().getId())
                .fromUserId(notification.getFromUser() != null ? notification.getFromUser().getId() : null)
                .fromUsername(fromUsername)
                .fromUserProfileImage(fromUserProfileImage)
                .activityId(notification.getActivityId())
                .postId(notification.getPostId())
                .offerId(notification.getOfferId())
                .locationId(notification.getLocationId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null)
                .build();
    }
}
