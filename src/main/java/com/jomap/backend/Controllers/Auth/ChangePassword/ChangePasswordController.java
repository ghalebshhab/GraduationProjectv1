package com.jomap.backend.Controllers.Auth.ChangePassword;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Auth.ChangePassword.ChangePasswordRequest;
import com.jomap.backend.Services.Auth.ChangePassword.ChangePasswordService;
import com.jomap.backend.Entities.Users.UserRepository; 
import com.jomap.backend.Entities.Users.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ChangePasswordController {

    private final ChangePasswordService changePasswordService;
    private final UserRepository userRepository; 

    public ChangePasswordController(ChangePasswordService changePasswordService, UserRepository userRepository) {
        this.changePasswordService = changePasswordService;
        this.userRepository = userRepository;
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.status(401).body(new ApiResponse<>(false, "المستخدم غير مسجل دخول", "Unauthorized"));
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود بالسيستم"));
        
        changePasswordService.changePassword(user.getId(), request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "تم تغيير كلمة السر بنجاح", "Success"));
    }
}