package com.start.demo.Controllers.Auth;

import com.start.demo.DTOs.Auth.Login.LoginRequest;
import com.start.demo.DTOs.Auth.Login.LoginResponse;
import com.start.demo.DTOs.Auth.Register.RegisterRequest;
import com.start.demo.DTOs.Auth.Register.RegisterResponse;
import com.start.demo.Services.Auth.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}