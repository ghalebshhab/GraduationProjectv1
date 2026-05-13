package com.jomap.backend.DTOs.Dashboard;

import lombok.Data;

@Data
public class AdminStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;

    private long totalLocations;
    private long approvedLocations;
    private long pendingLocations;
    private long inactiveLocations;

    private long totalPosts;
    private long activePosts;
    private long deletedPosts;

    private long totalReports;
    private long pendingReports;
    private long resolvedReports;
}