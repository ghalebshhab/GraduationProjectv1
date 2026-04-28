package com.jomap.backend.Controllers.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.Login.AdminLoginRequest;
import com.jomap.backend.DTOs.Dashboard.Login.AdminLoginResponse;
import com.jomap.backend.Services.Dashboard.Auth.AdminAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            @RequestBody AdminLoginRequest request
    ) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }
}