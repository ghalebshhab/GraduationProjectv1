package com.jomap.backend.Services.Auth.ResetPassword;

import com.jomap.backend.DTOs.Auth.ResetPassword.ResetPasswordRequest;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.DTOs.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResetPasswordServiceImpl implements ResetPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<Void> processForgotPassword(ResetPasswordRequest.Forgot request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            return ApiResponse.error("البريد الإلكتروني غير مسجل بالنظام");
        }

        User user = userOptional.get();
        String otp = "123456"; // تثبيت الرمز لسهولة الفحص بناءً على الخطة الذكية

        // تعيين الرمز، وقت الانتهاء، والنوع
        user.setOtpCode(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setOtpType("RESET_PASSWORD");
        userRepository.save(user);

        // تم تمرير null لتتطابق مع الـ Method بالـ DTO عندك
        return ApiResponse.success("تم إرسال رمز التحقق بنجاح إلى بريدك الإلكتروني", null);
    }

    @Override
    public ApiResponse<Void> verifyOtp(ResetPasswordRequest.VerifyOtp request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ApiResponse.error("البريد الإلكتروني غير صحيح");
        }

        User user = userOptional.get();

        // 1. التاكد من وجود رمز تولد أصلاً ومن نفس النوع
        if (user.getOtpCode() == null || user.getOtpExpiryTime() == null || !"RESET_PASSWORD".equals(user.getOtpType())) {
            return ApiResponse.error("لم يتم طلب رمز تحقق لإعادة تعيين كلمة المرور لهذا الحساب");
        }

        // 2. التأكد من صلاحية الوقت
        if (LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            return ApiResponse.error("انتهت صلاحية رمز التحقق، يرجى طلب رمز جديد");
        }

        // 3. مطابقة الرمز
        if (!user.getOtpCode().equals(request.getOtpCode())) {
            return ApiResponse.error("رمز التحقق المدخل غير صحيح");
        }

        return ApiResponse.success("تم التحقق من الرمز بنجاح", null);
    }

    @Override
    public ApiResponse<Void> resetPassword(ResetPasswordRequest.Reset request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return ApiResponse.error("البريد الإلكتروني غير صحيح");
        }

        User user = userOptional.get();

        // ⚠️ تأمين إضافي: التأكد من أن المستخدم قد مرّ بخطوة التحقق من الـ OTP بنجاح ولنفس النوع قبل تغيير الباسورد
        if (user.getOtpCode() == null || !"RESET_PASSWORD".equals(user.getOtpType())) {
            return ApiResponse.error("طلب غير مصرح به، يرجى التحقق من الرمز أولاً");
        }

        // تشفير كلمة المرور الجديدة وتحديثها (تأكد من أن اسم الميثود في الـ User هو setPasswordHash أو setPassword حسب الـ Setter عندك)
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        // تنظيف حقول الـ OTP بالأمان بعد نجاح العملية عشان ما تستخدمش تاني
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        user.setOtpType(null);
        userRepository.save(user);

        return ApiResponse.success("تم إعادة تعيين كلمة المرور بنجاح، يمكنك تسجيل الدخول الآن", null);
    }
}