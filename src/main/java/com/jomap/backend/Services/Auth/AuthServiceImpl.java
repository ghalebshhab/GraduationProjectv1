package com.jomap.backend.Services.Auth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.DTOs.Auth.Register.RegisterResponse;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserProfile;
import com.jomap.backend.Entities.Users.UserProfileRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Notefications.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public ApiResponse<RegisterResponse> register(RegisterRequest request) {

        if (request.getPhoneNumber() == null ||
                (!request.getPhoneNumber().matches("^\\+9627\\d{8}$")
                        && !request.getPhoneNumber().matches("^07\\d{8}$"))) {
            return ApiResponse.error("Phone number must be like +9627XXXXXXXX or 07XXXXXXXX");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("UserName is already used");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            return ApiResponse.error("Phone number is already used");
        }

        User user = new User();
        user.setEmail(request.getEmail().trim());
        user.setUsername(request.getUsername().trim());
        user.setPhoneNumber(request.getPhoneNumber().trim());
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

        RegisterResponse response = new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getRole().name()
        );

        return ApiResponse.success("Registered successfully", response);
    }

    @Override
    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isEmpty()) {
            return ApiResponse.error("User with this email does not exist");
        }

        User user = optionalUser.get();

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            return ApiResponse.error("Incorrect password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ApiResponse.error("This user account is not active");
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

        return ApiResponse.success("Logged in successfully", response);
    }
}