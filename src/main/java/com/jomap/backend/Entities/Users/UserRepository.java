package com.jomap.backend.Entities.Users;

import com.jomap.backend.Entities.Auth.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber( String phoneNumber);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    long countByIsActiveTrue();

    long countByIsActiveFalse();
}
