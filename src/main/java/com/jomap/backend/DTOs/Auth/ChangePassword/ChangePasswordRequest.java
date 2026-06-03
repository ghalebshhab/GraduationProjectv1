package com.jomap.backend.DTOs.Auth.ChangePassword;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
