package com.jomap.backend.DTOs.Dashboard;

import lombok.Data;

@Data
public class AdminUserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private Boolean active;
}