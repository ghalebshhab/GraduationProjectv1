package com.jomap.backend.Services.Auth.ResetPassword;

import com.jomap.backend.DTOs.Auth.ResetPassword.ResetPasswordRequest;
import com.jomap.backend.DTOs.ApiResponse; 

public interface ResetPasswordService {
    ApiResponse processForgotPassword(ResetPasswordRequest.Forgot request);
    ApiResponse verifyOtp(ResetPasswordRequest.VerifyOtp request);
    ApiResponse resetPassword(ResetPasswordRequest.Reset request);
}