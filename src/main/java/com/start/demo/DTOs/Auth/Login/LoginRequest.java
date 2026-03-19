package com.start.demo.DTOs.Auth.Login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;


}