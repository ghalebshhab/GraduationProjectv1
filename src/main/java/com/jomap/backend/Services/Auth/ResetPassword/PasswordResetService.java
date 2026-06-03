package com.jomap.backend.Services.Auth.ResetPassword;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.ChangePassword.ChangePasswordRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.ForgotPasswordRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.ResetPasswordRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.VerifyOtpRequest;
import com.jomap.backend.DTOs.Auth.ForgetPassword.VerifyOtpResponse;
import com.jomap.backend.Entities.Auth.Passwordreset;
import com.jomap.backend.Entities.Auth.Passwordresetrepo;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Notefications.EmailService;
import com.jomap.backend.Services.Notefications.SmsSenderService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final Passwordresetrepo otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailSenderService;
    private final SmsSenderService smsSenderService;

    private static final int OTP_EXPIRY_MINUTES = 1;
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    @Transactional
    public ApiResponse<Void> sendOtp(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // أمنياً: لا تكشف إذا الإيميل موجود أو لا
        if (user == null) {
            return new ApiResponse<>(true, "If this email exists, OTP has been sent.", null);
        }

        // منع إرسال OTP جديد بسرعة قبل انتهاء القديم
        var existingOtp = otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user);

        if (existingOtp.isPresent()) {
            Passwordreset oldOtp = existingOtp.get();

            if (oldOtp.getExpiresAt().isAfter(LocalDateTime.now())) {
                return new ApiResponse<>(false, "OTP already sent. Please wait 1 minute before requesting a new one.", null);
            }

            oldOtp.setUsed(true);
            otpRepository.save(oldOtp);
        }

        String otp = generateOtp();

        Passwordreset resetOtp = new Passwordreset();
        resetOtp.setUser(user);
        resetOtp.setOtpHash(passwordEncoder.encode(otp));
        resetOtp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        resetOtp.setUsed(false);
        resetOtp.setAttempts(0);

        otpRepository.save(resetOtp);

//        boolean emailSent = emailSenderService.sendPasswordResetOtp(user.getEmail(), otp);
//
//        if (!emailSent) {
//            return new ApiResponse<>(false, "OTP created but failed to send email.", null);
//        }
//
//        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
//            smsSenderService.sendPasswordResetOtp(user.getPhoneNumber(), otp);
//        }


        try {
            emailSenderService.sendPasswordResetOtp(user.getEmail(), otp);
        } catch (Exception e) {
            System.out.println("Email failed but request will continue: " + e.getMessage());
        }
        smsSenderService.sendPasswordResetOtp(user.getPhoneNumber(), otp);

        return new ApiResponse<>(true, "OTP generated successfully.", null); }

    @Transactional
    public ApiResponse<VerifyOtpResponse> verifyOtp(VerifyOtpRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return new ApiResponse<>(false, "Invalid OTP or email.", null);
        }

        Passwordreset resetOtp = otpRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElse(null);

        if (resetOtp == null) {
            return new ApiResponse<>(false, "No active OTP found. Please request a new OTP.", null);
        }

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            resetOtp.setUsed(true);
            otpRepository.save(resetOtp);
            return new ApiResponse<>(false, "OTP expired. Please request a new OTP.", null);
        }

        if (resetOtp.getAttempts() >= MAX_ATTEMPTS) {
            resetOtp.setUsed(true);
            otpRepository.save(resetOtp);
            return new ApiResponse<>(false, "Too many wrong attempts. Please request a new OTP.", null);
        }

        boolean otpCorrect = passwordEncoder.matches(request.getOtp(), resetOtp.getOtpHash());

        if (!otpCorrect) {
            resetOtp.setAttempts(resetOtp.getAttempts() + 1);
            otpRepository.save(resetOtp);
            return new ApiResponse<>(false, "Invalid OTP.", null);
        }

        String resetToken = UUID.randomUUID().toString();
        resetOtp.setResetTokenHash(passwordEncoder.encode(resetToken));
        resetOtp.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES));

        otpRepository.save(resetOtp);

        return new ApiResponse<>(
                true,
                "OTP verified successfully.",
                new VerifyOtpResponse(resetToken)
        );
    }

    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest request) {

        var activeOtps = otpRepository.findAll();

        Passwordreset matchedOtp = null;

        for (Passwordreset otp : activeOtps) {
            if (!otp.isUsed()
                    && otp.getResetTokenHash() != null
                    && passwordEncoder.matches(request.getResetToken(), otp.getResetTokenHash())) {
                matchedOtp = otp;
                break;
            }
        }

        if (matchedOtp == null) {
            return new ApiResponse<>(false, "Invalid reset token.", null);
        }

        if (matchedOtp.getResetTokenExpiresAt() == null ||
                matchedOtp.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            matchedOtp.setUsed(true);
            otpRepository.save(matchedOtp);
            return new ApiResponse<>(false, "Reset token expired. Please request a new OTP.", null);
        }

        User user = matchedOtp.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        matchedOtp.setUsed(true);
        otpRepository.save(matchedOtp);

        return new ApiResponse<>(true, "Password reset successfully.", null);
    }
    @Transactional
    public ApiResponse<String> changePassword(ChangePasswordRequest request, String emailFromToken) {

        Optional<User> opUser = userRepository.findByEmail(emailFromToken);

        if (opUser.isEmpty()) {
            return new ApiResponse<>(false, "Invalid email.", null);
        }
        User user = opUser.get();



        // Check old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return new ApiResponse<>(false, "Old password is incorrect", null);
        }

        // Optional: prevent same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            return new ApiResponse<>(false, "New password cannot be the same as old password", null);
        }

        // Save new password encoded
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse<>(true, "Password changed successfully", null);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}