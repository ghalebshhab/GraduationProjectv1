package com.jomap.backend.Services.Dashboard.Auth;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.Login.AdminLoginRequest;
import com.jomap.backend.DTOs.Dashboard.Login.AdminLoginResponse;

public interface AdminAuthService {

    ApiResponse<AdminLoginResponse> login(AdminLoginRequest request);
}