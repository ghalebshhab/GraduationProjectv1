package com.start.demo.Controllers.Auth;

import com.start.demo.DTOs.Auth.Login.LoginRequest;
import com.start.demo.DTOs.Auth.Login.LoginResponse;
import com.start.demo.DTOs.Auth.Register.RegisterRequest;
import com.start.demo.DTOs.Auth.Register.RegisterResponse;
import com.start.demo.DTOs.Common.ApiResponse;
import com.start.demo.Services.Auth.AuthService;
import com.start.demo.Utils.ResponseFactory;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);

        if (response == null) {
            return ResponseFactory.badRequest("Unable to register user. Check if email or username already exists.");
        }

        return ResponseFactory.created(response, "User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        if (response == null) {
            return ResponseFactory.badRequest("Invalid email or password");
        }

        return ResponseFactory.ok(response, "Login successful");
    }
}
