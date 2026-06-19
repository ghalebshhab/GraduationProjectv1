package com.jomap.backend.Services.Users;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.BlockedLocationResponse;
import com.jomap.backend.DTOs.Users.BlockedUserResponse;

import java.util.List;

public interface UserBlockService {
    ApiResponse<String> blockUser(String blockerEmail, Long blockedUserId);
    ApiResponse<String> unblockUser(String blockerEmail, Long blockedUserId);
    ApiResponse<List<BlockedUserResponse>> getBlockedUsers(String blockerEmail);
    ApiResponse<List<BlockedLocationResponse>> getBlockedLocations(String blockerEmail);
}
