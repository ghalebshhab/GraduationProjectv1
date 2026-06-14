package com.jomap.backend.Entities.Notifications;

import com.jomap.backend.Entities.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByToUserOrderByCreatedAtDesc(User toUser);
    
    List<Notification> findByToUserAndCategoryOrderByCreatedAtDesc(User toUser, NotificationCategory category);
    
    Long countByToUserAndIsReadFalse(User toUser);
}
