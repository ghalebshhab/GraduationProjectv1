package com.jomap.backend.Services.Dashboard.Auth;


import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.Login.AdminLoginRequest;
import com.jomap.backend.DTOs.Dashboard.Login.AdminLoginResponse;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Auth.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;



    @Override
    public ApiResponse<AdminLoginResponse> login(AdminLoginRequest request) {

        if (request == null) {
            return ApiResponse.error("Request body is required");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ApiResponse.error("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ApiResponse.error("Password is required");
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ApiResponse.error("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ApiResponse.error("Invalid email or password");
        }

        if (user.getRole() == null || !user.getRole().toString().equals("ADMIN")) {
            return ApiResponse.error("Access denied. Admin account is required.");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            return ApiResponse.error("This admin account is blocked.");
        }

        String token = jwtService.generateToken(user.getEmail());

        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(token);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().toString());

        return ApiResponse.success("Admin login successful", response);
    }
}