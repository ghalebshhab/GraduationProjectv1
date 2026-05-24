package com.jomap.backend.Entities.Auth;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Table(name = "password_reset")
@Data
public class Passwordreset {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // نفس المستخدم
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        // نخزن OTP مشفّر، لا تخزنه plain text
        @Column(nullable = false)
        private String otpHash;

        @Column(nullable = false)
        private LocalDateTime expiresAt;

        @Column(nullable = false)
        private LocalDateTime createdAt;

        @Column(nullable = false)
        private boolean used = false;

        @Column(nullable = false)
        private int attempts = 0;

        // بعد ما يتحقق من OTP نعطيه resetToken مؤقت
        private String resetTokenHash;

        private LocalDateTime resetTokenExpiresAt;

        @PrePersist
        public void prePersist() {
            createdAt = LocalDateTime.now();
        }
    }

