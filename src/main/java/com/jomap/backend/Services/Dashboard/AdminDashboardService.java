package com.jomap.backend.Services.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.AdminPostResponse;
import com.jomap.backend.DTOs.Dashboard.AdminReportResponse;
import com.jomap.backend.DTOs.Dashboard.AdminStatsResponse;
import com.jomap.backend.DTOs.Dashboard.AdminUserResponse;
import com.jomap.backend.DTOs.Locations.LocationResponse;

import java.util.List;

public interface AdminDashboardService {
    ApiResponse<AdminStatsResponse> getStats();

    ApiResponse<List<AdminUserResponse>> getUsers();

    ApiResponse<AdminUserResponse> blockUser(Long userId);

    ApiResponse<AdminUserResponse> unblockUser(Long userId);

    ApiResponse<List<LocationResponse>> getPendingLocations();

    ApiResponse<List<LocationResponse>> getAllLocations();

    ApiResponse<LocationResponse> approveLocation(Long locationId);

    ApiResponse<LocationResponse> rejectLocation(Long locationId, String reason);

    ApiResponse<LocationResponse> deactivateLocation(Long locationId);

    ApiResponse<List<AdminPostResponse>> getPosts();

    ApiResponse<AdminPostResponse> deletePost(Long postId);

    ApiResponse<List<AdminReportResponse>> getReports();

    ApiResponse<AdminReportResponse> resolveReport(Long reportId);
}
