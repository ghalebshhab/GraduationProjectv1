package com.jomap.backend.Controllers.Auth.ResetPassword;

import com.jomap.backend.DTOs.Auth.ResetPassword.ResetPasswordRequest;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Auth.ResetPassword.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {

    private final ResetPasswordService resetPasswordService;

    // 1. Endpoint طلب الرمز (Forgot Password)
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ResetPasswordRequest.Forgot request) {
        ApiResponse response = resetPasswordService.processForgotPassword(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // 2. Endpoint التحقق من الرمز (Verify OTP)
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody ResetPasswordRequest.VerifyOtp request) {
        ApiResponse response = resetPasswordService.verifyOtp(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    // 3. Endpoint تعيين كلمة المرور الجديدة (Reset Password)
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest.Reset request) {
        ApiResponse response = resetPasswordService.resetPassword(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }
}