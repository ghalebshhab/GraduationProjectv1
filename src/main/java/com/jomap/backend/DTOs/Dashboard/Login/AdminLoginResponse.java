package com.jomap.backend.DTOs.Dashboard.Login;

import lombok.Data;

@Data
public class AdminLoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String email;
    private String username;
    private String role;
}