package com.jomap.backend.DTOs.Dashboard;

import lombok.Data;

@Data
public class AdminReportResponse {

    private Long id;
    private String reason;
    private Boolean resolved;
    private Long reportedById;
    private String reportedBy;
}