package com.jomap.backend.Services.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.AdminPostResponse;
import com.jomap.backend.DTOs.Dashboard.AdminReportResponse;
import com.jomap.backend.DTOs.Dashboard.AdminStatsResponse;
import com.jomap.backend.DTOs.Dashboard.AdminUserResponse;
import com.jomap.backend.DTOs.Places.PlaceResponse;

import java.util.List;

public interface AdminDashboardService {
    ApiResponse<AdminStatsResponse> getStats();

    ApiResponse<List<AdminUserResponse>> getUsers();

    ApiResponse<AdminUserResponse> blockUser(Long userId);

    ApiResponse<AdminUserResponse> unblockUser(Long userId);

    ApiResponse<List<PlaceResponse>> getPendingPlaces();

    ApiResponse<List<PlaceResponse>> getAllPlaces();

    ApiResponse<PlaceResponse> approvePlace(Long placeId);

    ApiResponse<PlaceResponse> rejectPlace(Long placeId);

    ApiResponse<PlaceResponse> deactivatePlace(Long placeId);

    ApiResponse<List<AdminPostResponse>> getPosts();

    ApiResponse<AdminPostResponse> deletePost(Long postId);

    ApiResponse<List<AdminReportResponse>> getReports();

    ApiResponse<AdminReportResponse> resolveReport(Long reportId);
}
