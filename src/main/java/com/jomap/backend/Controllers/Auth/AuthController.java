package com.jomap.backend.Controllers.Auth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.DTOs.Auth.social.SocialLoginRequest;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Auth.AuthService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));

    }
    @PutMapping("/make-admin")
    public ApiResponse<String> makeAdmin(@RequestParam String email) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();
        user.setRole(Role.ADMIN);

        userRepository.save(user);

        return ApiResponse.success("User is now ADMIN", email);
    }
//    @PostMapping("/google")
//    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody SocialLoginRequest request) {
//        return ResponseEntity.ok(authService.loginWithGoogle(request.token()));
//    }
//
//    @PostMapping("/facebook")
//    public ResponseEntity<ApiResponse<LoginResponse>> facebookLogin(@RequestBody SocialLoginRequest request) {
//        return ResponseEntity.ok(authService.loginWithFacebook(request.token()));
//    }
}