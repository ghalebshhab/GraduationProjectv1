package com.jomap.backend.DTOs.Auth.ResetPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class ResetPasswordRequest {

    // 1. طلب الرمز (Forgot Password)
    @Data
    public static class Forgot {
        @NotBlank(message = "البريد الإلكتروني مطلوب")
        @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
        private String email;
    }

    // 2. التحقق من الرمز (Verify OTP)
    @Data
    public static class VerifyOtp {
        @NotBlank(message = "البريد الإلكتروني مطلوب")
        @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
        private String email;

        @NotBlank(message = "رمز التحقق مطلوب")
        @Pattern(regexp = "^\\d{6}$", message = "يجب أن يتكون رمز التحقق من 6 أرقام فقط")
        private String otpCode;
    }

    // 3. إعادة تعيين الباسورد (Reset Password)
    @Data
    public static class Reset {
        @NotBlank(message = "البريد الإلكتروني مطلوب")
        @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
        private String email;

        @NotBlank(message = "كلمة المرور الجديدة مطلوبة")
        @Size(min = 6, message = "يجب أن لا تقل كلمة المرور عن 6 خانات")
        private String newPassword;
    }
}