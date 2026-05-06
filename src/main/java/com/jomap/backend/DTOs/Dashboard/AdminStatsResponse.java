package com.jomap.backend.DTOs.Dashboard;

import lombok.Data;

@Data
public class AdminStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;

    private long totalPlaces;
    private long approvedPlaces;
    private long pendingPlaces;
    private long inactivePlaces;

    private long totalPosts;
    private long activePosts;
    private long deletedPosts;

    private long totalReports;
    private long pendingReports;
    private long resolvedReports;
}