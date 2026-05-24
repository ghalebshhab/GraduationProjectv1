package com.jomap.backend.Entities.Auth;

import com.jomap.backend.Entities.Users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface Passwordresetrepo extends JpaRepository<Passwordreset,Long> {
    Optional<Passwordreset> findTopByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    Optional<Passwordreset> findTopByResetTokenHashAndUsedFalseOrderByCreatedAtDesc(String resetTokenHash);
}
