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
import com.jomap.backend.Entities.Feedback.Feedback;
import com.jomap.backend.Entities.Feedback.FeedbackRepository;
import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final ActivityRepository activityRepository;
    private final LocationRepo locationRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            FeedbackRepository feedbackRepository,
            ActivityRepository activityRepository,
            LocationRepo locationRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.feedbackRepository = feedbackRepository;
        this.activityRepository = activityRepository;
        this.locationRepository = locationRepository;
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

        // Delete old notification of the same type between the same users/entities to avoid duplicates
        if (fromUser != null) {
            List<Notification> duplicates = notificationRepository.findByToUserAndFromUserAndType(toUserOpt.get(), fromUser, type);
            List<Notification> toDelete = duplicates.stream().filter(old -> {
                if (request.getPostId() != null ? !request.getPostId().equals(old.getPostId()) : old.getPostId() != null) return false;
                if (request.getActivityId() != null ? !request.getActivityId().equals(old.getActivityId()) : old.getActivityId() != null) return false;
                if (request.getOfferId() != null ? !request.getOfferId().equals(old.getOfferId()) : old.getOfferId() != null) return false;
                if (request.getLocationId() != null ? !request.getLocationId().equals(old.getLocationId()) : old.getLocationId() != null) return false;
                if (request.getReviewId() != null ? !request.getReviewId().equals(old.getReviewId()) : old.getReviewId() != null) return false;
                return true;
            }).collect(Collectors.toList());

            if (!toDelete.isEmpty()) {
                notificationRepository.deleteAll(toDelete);
            }
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
                .reviewId(request.getReviewId())
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        return ApiResponse.success("تم إرسال الإشعار بنجاح", mapToResponse(saved));
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> getUserNotificationsByCategory(String email, String categoryParam) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        if ("ALL".equalsIgnoreCase(categoryParam)) {
            return getUserNotifications(email);
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

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> getNotificationsByLocationId(Long locationId) {
        List<NotificationResponse> responses = notificationRepository
                .findByLocationIdOrderByCreatedAtDesc(locationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("تم جلب إشعارات المنشأة بنجاح", responses);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationResponse>> getNotificationsByActivityId(Long activityId) {
        List<NotificationResponse> responses = notificationRepository
                .findByActivityIdAndTypeNotOrderByCreatedAtDesc(activityId, NotificationType.ACTIVITY_REGISTRATION)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ApiResponse.success("تم جلب إشعارات الفعالية بنجاح", responses);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        String fromUsername = null;
        String fromUserProfileImage = null;
        String ownerReply = null;
        Long reviewId = notification.getReviewId();

        if (reviewId == null && notification.getType() == NotificationType.REVIEW
                && notification.getFromUser() != null && notification.getLocationId() != null) {
            try {
                Optional<Feedback> oldFeedbackOpt = feedbackRepository.findFirstByUser_IdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(
                        notification.getFromUser().getId(), com.jomap.backend.Entities.Feedback.TargetType.LOCATION, notification.getLocationId());
                if (oldFeedbackOpt.isPresent()) {
                    reviewId = oldFeedbackOpt.get().getId();
                }
            } catch (Exception e) {
                System.out.println("Error fetching old feedback for notification: " + e.getMessage());
            }
        }

        if (reviewId != null) {
            try {
                Optional<Feedback> feedbackOpt = feedbackRepository.findById(reviewId);
                if (feedbackOpt.isPresent()) {
                    ownerReply = feedbackOpt.get().getOwnerReply();
                }
            } catch (Exception e) {
                System.out.println("Error fetching feedback info for notification: " + e.getMessage());
            }
        }

        String activityName = null;
        String activityImage = null;
        if (notification.getActivityId() != null) {
            try {
                Optional<Activity> activityOpt = activityRepository.findById(notification.getActivityId());
                if (activityOpt.isPresent()) {
                    activityName = activityOpt.get().getTitle();
                    activityImage = activityOpt.get().getImageUrl();
                }
            } catch (Exception e) {
                System.out.println("Error fetching activity info for notification: " + e.getMessage());
            }
        }

        String locationName = null;
        String locationStatus = null;
        String rejectionReason = notification.getRejectionReason();
        if (notification.getLocationId() != null) {
            try {
                Optional<LocationList> locationOpt = locationRepository.findById(notification.getLocationId());
                if (locationOpt.isPresent()) {
                    LocationList location = locationOpt.get();
                    locationName = location.getName();
                    locationStatus = location.getStatus() == null ? null : location.getStatus().name();
                    if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                        rejectionReason = location.getRejectionReason();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error fetching location info for notification: " + e.getMessage());
            }
        }

        if (notification.getFromUser() != null) {
            try {
                User fromUser = notification.getFromUser();
                fromUsername = fromUser.getUsername();
                if (fromUser.getProfile() != null && fromUser.getProfile().getFirstName() != null) {
                    fromUsername = fromUser.getProfile().getFirstName() + " " + fromUser.getProfile().getLastName();
                }

                if (fromUser.getProfileImageUrl() != null) {
                    fromUserProfileImage = fromUser.getProfileImageUrl();
                } else if (fromUser.getProfile() != null && fromUser.getProfile().getProfileImageUrl() != null) {
                    fromUserProfileImage = fromUser.getProfile().getProfileImageUrl();
                }
            } catch (Exception e) {
                System.out.println("Error fetching profile info for notification: " + e.getMessage());
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
                .activityName(activityName)
                .activityImage(activityImage)
                .postId(notification.getPostId())
                .offerId(notification.getOfferId())
                .locationId(notification.getLocationId())
                .locationName(locationName)
                .locationStatus(locationStatus)
                .rejectionReason(rejectionReason)
                .reviewId(reviewId)
                .ownerReply(ownerReply)
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null)
                .build();
    }
}
