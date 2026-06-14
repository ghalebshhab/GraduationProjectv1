package com.jomap.backend.Entities.Users;

import com.jomap.backend.Entities.Auth.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.jomap.backend.Entities.Users.Role;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<User> findByEmail(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    Optional<User> findByUsername(@org.springframework.data.repository.query.Param("username") String username);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmail(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(u) > 0 FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    boolean existsByUsername(@org.springframework.data.repository.query.Param("username") String username);

    boolean existsByPhoneNumber( String phoneNumber);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);

    long countByIsActiveTrue();

    long countByIsActiveFalse();

    List<User> findTop10ByIsActiveTrueAndRoleNotOrderByIdDesc(Role role);
}
