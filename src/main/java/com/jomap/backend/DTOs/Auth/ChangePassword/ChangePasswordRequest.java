package com.jomap.backend.DTOs.Auth.ChangePassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data; 

@Data 
public class ChangePasswordRequest {

    @NotBlank(message = "كلمة السر الحالية مطلوبة")
    private String currentPassword;

    @NotBlank(message = "كلمة السر الجديدة مطلوبة")
    @Size(min = 6, message = "كلمة السر الجديدة يجب أن لا تقل عن 6 خانات")
    private String newPassword;
}