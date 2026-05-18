package com.jomap.backend.DTOs.Auth.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SocialLoginRequest {

    @NotBlank(message = "Token is required")
    private String token;
}

