package com.jomap.backend.Controllers.Auth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.Login.LoginResponse;
import com.jomap.backend.DTOs.Auth.social.SocialLoginRequest;
import com.jomap.backend.Services.Auth.SocialAuth.SocialAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:3000",
        "https://jomab-712232187160.europe-west1.run.app"
})
public class SocialController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
            @Valid @RequestBody SocialLoginRequest request
    ) {
        ApiResponse<LoginResponse> response =
                socialAuthService.loginWithGoogle(request.getToken());

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<LoginResponse>> facebookLogin(
            @Valid @RequestBody SocialLoginRequest request
    ) {
        ApiResponse<LoginResponse> response =
                socialAuthService.loginWithFacebook(request.getToken());

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}