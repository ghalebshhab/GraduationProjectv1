package com.jomap.backend.Services.Dashboard;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Dashboard.AdminPostResponse;
import com.jomap.backend.DTOs.Dashboard.AdminReportResponse;
import com.jomap.backend.DTOs.Dashboard.AdminStatsResponse;
import com.jomap.backend.DTOs.Dashboard.AdminUserResponse;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Locations.LocationStatus;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Reports.Report;
import com.jomap.backend.Entities.Reports.ReportRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final LocationRepo locationRepository;
    private final PostRepository postRepository;
    private final ReportRepository reportRepository;

    @Override
    public ApiResponse<AdminStatsResponse> getStats() {

        AdminStatsResponse response = new AdminStatsResponse();

        response.setTotalUsers(userRepository.count());
        response.setActiveUsers(userRepository.countByIsActiveTrue());
        response.setBlockedUsers(userRepository.countByIsActiveFalse());

        response.setTotalLocations(locationRepository.count());
        response.setApprovedLocations(locationRepository.countByStatus(LocationStatus.PUBLISHED));
        response.setPendingLocations(locationRepository.countByStatus(LocationStatus.PENDING));
        response.setInactiveLocations(
                locationRepository.countByStatus(LocationStatus.REJECTED)
                        + locationRepository.countByStatus(LocationStatus.DEACTIVATED)
                        + locationRepository.countByStatus(LocationStatus.DELETED)
        );

        response.setTotalPosts(postRepository.count());
        response.setActivePosts(postRepository.countByIsDeletedFalse());
        response.setDeletedPosts(postRepository.countByIsDeletedTrue());

        response.setTotalReports(reportRepository.count());
        response.setPendingReports(reportRepository.countByResolvedFalse());
        response.setResolvedReports(reportRepository.countByResolvedTrue());

        return ApiResponse.success("Admin dashboard stats fetched successfully", response);
    }

    @Override
    public ApiResponse<List<AdminUserResponse>> getUsers() {

        List<AdminUserResponse> response = userRepository.findAll()
                .stream()
                .map(this::mapUserToAdminResponse)
                .toList();

        return ApiResponse.success("Users fetched successfully", response);
    }

    @Override
    public ApiResponse<AdminUserResponse> blockUser(Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();
        user.setIsActive(false);

        User savedUser = userRepository.save(user);

        return ApiResponse.success("User blocked successfully", mapUserToAdminResponse(savedUser));
    }

    @Override
    public ApiResponse<AdminUserResponse> unblockUser(Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOptional.get();
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        return ApiResponse.success("User unblocked successfully", mapUserToAdminResponse(savedUser));
    }

    @Override
    public ApiResponse<List<LocationResponse>> getPendingLocations() {

        List<LocationResponse> response = locationRepository.findByStatusOrderByIdDesc(LocationStatus.PENDING)
                .stream()
                .map(this::mapLocationToResponse)
                .toList();

        return ApiResponse.success("Pending locations fetched successfully", response);
    }

    @Override
    public ApiResponse<List<LocationResponse>> getAllLocations() {

        List<LocationResponse> response = locationRepository.findAll()
                .stream()
                .map(this::syncLocationApprovalFlags)
                .map(this::mapLocationToResponse)
                .toList();

        return ApiResponse.success("All locations fetched successfully", response);
    }

    @Override
    public ApiResponse<LocationResponse> approveLocation(Long locationId) {

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);

        if (locationOptional.isEmpty()) {
            return ApiResponse.error("Location not found");
        }

        LocationList location = locationOptional.get();
        location.setStatus(LocationStatus.PUBLISHED);
        location.setApproved(true);
        location.setActive(true);
        location.setRejectionReason(null);

        LocationList savedLocation = locationRepository.save(location);

        return ApiResponse.success("Location approved and published successfully", mapLocationToResponse(savedLocation));
    }

    @Override
    public ApiResponse<LocationResponse> rejectLocation(Long locationId, String reason) {

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);

        if (locationOptional.isEmpty()) {
            return ApiResponse.error("Location not found");
        }

        LocationList location = locationOptional.get();
        location.setStatus(LocationStatus.REJECTED);
        location.setApproved(false);
        location.setActive(false);
        location.setRejectionReason(reason);

        LocationList savedLocation = locationRepository.save(location);

        return ApiResponse.success("Location rejected successfully", mapLocationToResponse(savedLocation));
    }

    @Override
    public ApiResponse<LocationResponse> deactivateLocation(Long locationId) {

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);

        if (locationOptional.isEmpty()) {
            return ApiResponse.error("Location not found");
        }

        LocationList location = locationOptional.get();
        location.setStatus(LocationStatus.DEACTIVATED);
        location.setActive(false);
        location.setApproved(false);

        LocationList savedLocation = locationRepository.save(location);

        return ApiResponse.success("Location deactivated successfully", mapLocationToResponse(savedLocation));
    }

    @Override
    public ApiResponse<List<AdminPostResponse>> getPosts() {

        List<AdminPostResponse> response = postRepository.findAll()
                .stream()
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() == null && p2.getCreatedAt() == null) {
                        return 0;
                    }

                    if (p1.getCreatedAt() == null) {
                        return 1;
                    }

                    if (p2.getCreatedAt() == null) {
                        return -1;
                    }

                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                })
                .map(this::mapPostToAdminResponse)
                .toList();

        return ApiResponse.success("Posts fetched successfully", response);
    }

    @Override
    public ApiResponse<AdminPostResponse> deletePost(Long postId) {

        Optional<Post> postOptional = postRepository.findById(postId);

        if (postOptional.isEmpty()) {
            return ApiResponse.error("Post not found");
        }

        Post post = postOptional.get();
        post.setIsDeleted(true);

        Post savedPost = postRepository.save(post);

        return ApiResponse.success("Post deleted successfully", mapPostToAdminResponse(savedPost));
    }

    @Override
    public ApiResponse<List<AdminReportResponse>> getReports() {

        List<AdminReportResponse> response = reportRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapReportToAdminResponse)
                .toList();

        return ApiResponse.success("Reports fetched successfully", response);
    }

    @Override
    public ApiResponse<AdminReportResponse> resolveReport(Long reportId) {

        Optional<Report> reportOptional = reportRepository.findById(reportId);

        if (reportOptional.isEmpty()) {
            return ApiResponse.error("Report not found");
        }

        Report report = reportOptional.get();
        report.setResolved(true);

        Report savedReport = reportRepository.save(report);

        return ApiResponse.success("Report resolved successfully", mapReportToAdminResponse(savedReport));
    }

    private AdminUserResponse mapUserToAdminResponse(User user) {

        AdminUserResponse response = new AdminUserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole() == null ? null : user.getRole().toString());
        response.setActive(user.getIsActive());

        return response;
    }

    private LocationList syncLocationApprovalFlags(LocationList location) {
        if (location.getStatus() == LocationStatus.PUBLISHED) {
            location.setApproved(true);
            location.setActive(true);
        } else if (location.getStatus() == LocationStatus.PENDING) {
            location.setApproved(false);
            location.setActive(true);
        } else if (location.getStatus() == LocationStatus.APPROVED) {
            location.setApproved(true);
            location.setActive(false);
        } else {
            location.setApproved(false);
            location.setActive(false);
        }

        return location;
    }

    private LocationResponse mapLocationToResponse(LocationList location) {

        LocationList normalizedLocation = syncLocationApprovalFlags(location);

        LocationResponse response = new LocationResponse();

        response.setLocationId(normalizedLocation.getId());
        response.setName(normalizedLocation.getName());
        response.setDescription(normalizedLocation.getDescription());
        response.setEmail(normalizedLocation.getEmail());
        response.setPhoneNumber(normalizedLocation.getPhoneNumber());
        response.setLogoUrl(normalizedLocation.getLogoUrl());
        response.setCoverUrl(normalizedLocation.getCoverUrl());
        response.setLatitude(normalizedLocation.getLatitude());
        response.setLongitude(normalizedLocation.getLongitude());
        if (normalizedLocation.getGovernorate() != null) {
            response.setGovernorateName(normalizedLocation.getGovernorate().getName());
            response.setGovernorateId(normalizedLocation.getGovernorate().getId());
        }
        response.setCategory(normalizedLocation.getCategory());
        response.setRating(normalizedLocation.getRating());
        response.setReviewCount(normalizedLocation.getReviewCount());
        response.setOwnerUpdate(normalizedLocation.getOwnerUpdate());
        response.setCreatedAt(normalizedLocation.getCreatedAt());
        response.setUpdatedAt(normalizedLocation.getUpdatedAt());

        if (normalizedLocation.getOwner() != null) {
            response.setOwnerId(normalizedLocation.getOwner().getId());
            response.setOwnerName(normalizedLocation.getOwner().getUsername());
        }

        response.setStatus(normalizedLocation.getStatus());
        response.setIsActive(normalizedLocation.getActive());
        response.setIsApproved(normalizedLocation.getApproved());
        response.setRejectionReason(normalizedLocation.getRejectionReason());

        return response;
    }

    private AdminPostResponse mapPostToAdminResponse(Post post) {

        AdminPostResponse response = new AdminPostResponse();

        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setType(post.getType() == null ? null : post.getType().toString());
        response.setDeleted(post.getIsDeleted());

        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorName(post.getAuthor().getUsername());
        }

        return response;
    }

    private AdminReportResponse mapReportToAdminResponse(Report report) {

        AdminReportResponse response = new AdminReportResponse();

        response.setId(report.getId());
        response.setReason(report.getReason());
        response.setResolved(report.getResolved());

        if (report.getReportedBy() != null) {
            response.setReportedById(report.getReportedBy().getId());
            response.setReportedBy(report.getReportedBy().getUsername());
        }

        return response;
    }
}
