package com.jomap.backend.Controllers.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.AdminPostResponse;
import com.jomap.backend.DTOs.Dashboard.AdminReportResponse;
import com.jomap.backend.DTOs.Dashboard.AdminStatsResponse;
import com.jomap.backend.DTOs.Dashboard.AdminUserResponse;
import com.jomap.backend.DTOs.Places.PlaceResponse;
import com.jomap.backend.Services.Dashboard.AdminDashboardService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@AllArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;



    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(adminDashboardService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getUsers() {
        return ResponseEntity.ok(adminDashboardService.getUsers());
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<ApiResponse<AdminUserResponse>> blockUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminDashboardService.blockUser(userId));
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<ApiResponse<AdminUserResponse>> unblockUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(adminDashboardService.unblockUser(userId));
    }

    @GetMapping("/places/pending")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getPendingPlaces() {
        return ResponseEntity.ok(adminDashboardService.getPendingPlaces());
    }

    @GetMapping("/places/all")
    public ResponseEntity<ApiResponse<List<PlaceResponse>>> getAllPlaces() {
        return ResponseEntity.ok(adminDashboardService.getAllPlaces());
    }

    @PutMapping("/places/{placeId}/approve")
    public ResponseEntity<ApiResponse<PlaceResponse>> approvePlace(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(adminDashboardService.approvePlace(placeId));
    }

    @PutMapping("/places/{placeId}/reject")
    public ResponseEntity<ApiResponse<PlaceResponse>> rejectPlace(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(adminDashboardService.rejectPlace(placeId));
    }

    @PutMapping("/places/{placeId}/deactivate")
    public ResponseEntity<ApiResponse<PlaceResponse>> deactivatePlace(
            @PathVariable Long placeId
    ) {
        return ResponseEntity.ok(adminDashboardService.deactivatePlace(placeId));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<AdminPostResponse>>> getPosts() {
        return ResponseEntity.ok(adminDashboardService.getPosts());
    }

    @PutMapping("/posts/{postId}/delete")
    public ResponseEntity<ApiResponse<AdminPostResponse>> deletePost(
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(adminDashboardService.deletePost(postId));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<AdminReportResponse>>> getReports() {
        return ResponseEntity.ok(adminDashboardService.getReports());
    }

    @PutMapping("/reports/{reportId}/resolve")
    public ResponseEntity<ApiResponse<AdminReportResponse>> resolveReport(
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(adminDashboardService.resolveReport(reportId));
    }
}