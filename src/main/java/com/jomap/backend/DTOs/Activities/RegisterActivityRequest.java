package com.jomap.backend.DTOs.Activities;

import lombok.Data;

@Data
public class RegisterActivityRequest {
    private Long governorateId;
    private String detailedAddress;
}
