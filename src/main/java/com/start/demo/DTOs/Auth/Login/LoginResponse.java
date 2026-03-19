package com.start.demo.DTOs.Auth.Login;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type = "Bearer";

    private Long id;
    private String email;
    private String username;
    private String role;



}