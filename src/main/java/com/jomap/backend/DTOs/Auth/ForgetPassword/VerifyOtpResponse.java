package com.jomap.backend.DTOs.Auth.ForgetPassword;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VerifyOtpResponse {
    private String resetToken;
}