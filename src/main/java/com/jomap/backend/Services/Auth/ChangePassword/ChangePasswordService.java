package com.jomap.backend.Services.Auth.ChangePassword;

import com.jomap.backend.DTOs.Auth.ChangePassword.ChangePasswordRequest;

public interface ChangePasswordService {
    void changePassword(Long userId, ChangePasswordRequest request);
}