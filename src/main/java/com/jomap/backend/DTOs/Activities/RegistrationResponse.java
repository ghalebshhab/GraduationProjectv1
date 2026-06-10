package com.jomap.backend.DTOs.Activities;

import com.jomap.backend.Entities.Activities.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String userEmail;
    private String username;
    private String phoneNumber;
    private String status;
    private String registrationDate;
    private String registrationTime;
    private String userImageUrl;
}
