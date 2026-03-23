package com.jomap.backend.DTOs.Auth.Register;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {

    private Long id;
    private String email;
    private String username;
    private String role;





}