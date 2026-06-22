package com.jomap.backend.Controllers.Auth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.ChangePassword.ChangePasswordRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.ForgotPasswordRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.ResetPasswordRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.VerifyOtpRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.VerifyOtpResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Auth.NormalAuth.AuthService;

import com.jomap.backend.Services.Auth.ResetPassword.PasswordResetService;
import com.jomap.backend.Services.Auth.TokenBlacklistService;
import com.jomap.backend.Services.Auth.JwtService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            jakarta.servlet.http.HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                java.util.Date expiration = jwtService.extractExpiration(token);
                tokenBlacklistService.blacklistToken(token, expiration);
            } catch (Exception e) {
                // Token is invalid or already expired - still treat as logout
            }
        }
        return ResponseEntity.ok(ApiResponse.success("تم تسجيل الخروج بنجاح", "Logged out successfully"));
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

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return passwordResetService.sendOtp(request);
    }

    @PostMapping("/verify-reset-otp")
    public ApiResponse<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return passwordResetService.verifyOtp(request);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return passwordResetService.resetPassword(request);
    }
    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        if (authentication == null) {
            return new ApiResponse<>(false, "Unauthorized: token is missing or invalid", null);
        }

        String email = authentication.getName();

        ApiResponse<String> response = passwordResetService.changePassword(request, email);

        if (!response.isSuccess()) {
            return response;
        }

        return response;
    }

    // @PostMapping("/google")
    // public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@RequestBody
    // SocialLoginRequest request) {
    // return ResponseEntity.ok(authService.loginWithGoogle(request.token()));
    // }
    //
    // @PostMapping("/facebook")
    // public ResponseEntity<ApiResponse<LoginResponse>> facebookLogin(@RequestBody
    // SocialLoginRequest request) {
    // return ResponseEntity.ok(authService.loginWithFacebook(request.token()));
    // }
}