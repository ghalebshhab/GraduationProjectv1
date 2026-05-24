package com.jomap.backend.Services.Auth.SocialAuth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;

public interface SocialAuthService {

    ApiResponse<LoginResponse> loginWithGoogle(String token);

    ApiResponse<LoginResponse> loginWithFacebook(String token);
}