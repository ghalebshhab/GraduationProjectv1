package com.start.demo.Services.Auth;


import com.start.demo.DTOs.Auth.Login.LoginRequest;
import com.start.demo.DTOs.Auth.Login.LoginResponse;
import com.start.demo.DTOs.Auth.Register.RegisterRequest;
import com.start.demo.DTOs.Auth.Register.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
