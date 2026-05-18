package com.jomap.backend.DTOs.Auth.social;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocialUserInfo {

    private String providerId;
    private String email;
    private String name;
    private String picture;
}