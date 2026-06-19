package com.jomap.backend.Controllers.Notifications;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Notifications.NotificationResponse;
import com.jomap.backend.Services.Notifications.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(notificationService.getUserNotifications(principal.getName()));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotificationsByCategory(
            @PathVariable String category, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(notificationService.getUserNotificationsByCategory(principal.getName(), category));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getUnreadCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        ApiResponse<Long> serviceResponse = notificationService.getUnreadCount(principal.getName());
        if (!serviceResponse.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.error(serviceResponse.getMessage()));
        }
        return ResponseEntity.ok(ApiResponse.success(serviceResponse.getMessage(), java.util.Map.of("count", serviceResponse.getData())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(notificationService.markAsRead(id, principal.getName()));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        return ResponseEntity.ok(notificationService.markAllAsRead(principal.getName()));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByLocationId(
            @PathVariable Long locationId) {
        return ResponseEntity.ok(notificationService.getNotificationsByLocationId(locationId));
    }
}
