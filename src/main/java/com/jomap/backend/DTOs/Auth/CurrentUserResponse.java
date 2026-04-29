package com.jomap.backend.DTOs.Auth;

import lombok.Data;

@Data
public class CurrentUserResponse {
    private Long id;
    private String email;
    private String username;
    private String role;
    private String profileImageUrl;
}