package com.jomap.backend.Services.Notefications;

public interface SmsSenderService {
    void sendPasswordResetOtp(String phoneNumber, String otp);
}