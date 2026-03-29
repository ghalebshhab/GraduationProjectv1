package com.jomap.backend.Services.Auth;


import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginRequest;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.Register.RegisterRequest;
import com.jomap.backend.DTOs.Auth.Register.RegisterResponse;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ApiResponse<RegisterResponse> register(RegisterRequest request);
    ApiResponse<LoginResponse>  login(LoginRequest request);
}
