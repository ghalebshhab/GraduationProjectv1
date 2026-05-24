package com.jomap.backend.DTOs.Auth.Register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "الاسم الأول مطلوب")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "اسم العائلة مطلوب")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "اسم المستخدم مطلوب")
    @Size(min = 3, max = 120)
    private String username;

    @NotBlank(message = "البريد الإلكتروني مطلوب")
    @Email(message = "صيغة البريد الإلكتروني غير صحيحة")
    private String email;

    @NotBlank(message = "رقم الهاتف مطلوب")
    private String phoneNumber;


    private String gender;

    private LocalDate dateOfBirth; 

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Size(min = 6, max = 100)
    private String password;
}