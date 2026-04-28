package com.jomap.backend.DTOs.Dashboard.Login;

import lombok.Data;

@Data
public class AdminLoginRequest {

    private String email;
    private String password;
}