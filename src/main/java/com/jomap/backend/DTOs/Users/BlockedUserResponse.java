package com.jomap.backend.DTOs.Users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BlockedUserResponse {
    private Long id;
    private String username;
    private String profileImageUrl;
    private String firstName;
    private String lastName;
}
