package com.jomap.backend.Services.Notifications;

import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Notifications.Notification;
import com.jomap.backend.Entities.Notifications.NotificationCategory;
import com.jomap.backend.Entities.Notifications.NotificationRepository;
import com.jomap.backend.Entities.Notifications.NotificationType;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LocationNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendLocationRejectedNotification(LocationList location, String reason) {
        if (location == null || location.getId() == null || location.getOwner() == null) {
            return;
        }

        Long ownerId = location.getOwner().getId();
        User owner = userRepository.findById(ownerId).orElse(null);
        if (owner == null) {
            return;
        }

        String locationName = location.getName() == null || location.getName().trim().isEmpty()
                ? "منشأتك"
                : location.getName().trim();

        String safeReason = reason == null || reason.trim().isEmpty()
                ? "لم يتم تحديد السبب. يرجى مراجعة بيانات المنشأة وإعادة المحاولة."
                : reason.trim();

        String text = "تم رفض طلب اعتماد منشأة \"" + locationName + "\". السبب: " + safeReason;

        Notification notification = Notification.builder()
                .text(text)
                .type(NotificationType.SYSTEM)
                .category(NotificationCategory.ALL)
                .toUser(owner)
                .locationId(location.getId())
                .isRead(false)
                .build();

        notificationRepository.saveAndFlush(notification);
    }
}
