package com.jomap.backend.Services.Auth;

import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.DTOs.Auth.Register.RegisterResponse;
import com.jomap.backend.Entities.Users.*;
import com.jomap.backend.Entities.Users.*;
import com.jomap.backend.Services.Notefications.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserProfileRepository userProfileRepository;

    @Override
    public ResponseEntity<?> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("Error", "Email already exists"));
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("Error", "Username already used "));
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Phone number already used"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);

        userProfileRepository.save(profile);

        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(
                new RegisterResponse(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getUsername(),
                        savedUser.getRole().name()
                )
        );
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "User with this email does not exist"));
        }

        User user = optionalUser.get();

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Incorrect password"));
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "User account is inactive"));
        }
        if (!userProfileRepository.existsByUserId(user.getId())) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            userProfileRepository.save(profile);
        }

        String token = jwtService.generateToken(user.getEmail());
        try {
            emailService.sendLoginSuccessEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);
    }
}