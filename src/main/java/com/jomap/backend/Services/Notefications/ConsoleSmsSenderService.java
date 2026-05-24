package com.jomap.backend.Services.Notefications;

import org.springframework.stereotype.Service;

@Service
public class ConsoleSmsSenderService implements SmsSenderService {

    @Override
    public void sendPasswordResetOtp(String phoneNumber, String otp) {
        System.out.println("Sending OTP to phone: " + phoneNumber + " OTP: " + otp);
    }
}