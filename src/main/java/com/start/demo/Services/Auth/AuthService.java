package com.start.demo.Services.Auth;


import com.start.demo.DTOs.Auth.Login.LoginRequest;
import com.start.demo.DTOs.Auth.Login.LoginResponse;
import com.start.demo.DTOs.Auth.Register.RegisterRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AuthService {
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
}
