package com.jomap.backend.Services.Auth.ChangePassword;

import com.jomap.backend.DTOs.Auth.ChangePassword.ChangePasswordRequest;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) { 
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

                System.out.println("المدخل الصافي من الموبايل: " + request.getCurrentPassword());
System.out.println("المشفر المخزن بالداتابيز: " + user.getPasswordHash());

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) { 
            throw new IllegalArgumentException("كلمة السر الحالية غير صحيحة");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}