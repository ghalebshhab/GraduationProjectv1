package com.jomap.backend.Services.Friendship;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.FriendShips.FriendshipResponse;

import java.util.List;

public interface FriendshipService {

    ApiResponse<FriendshipResponse> sendFriendRequest(String senderEmail, Long receiverId);
    ApiResponse<FriendshipResponse> acceptFriendRequest(Long friendshipId, Long receiverId);

    ApiResponse<String> rejectFriendRequest(Long friendshipId, Long receiverId);

    ApiResponse<String> cancelFriendRequest(Long friendshipId, Long senderId);

    ApiResponse<List<FriendshipResponse>> getPendingRequests(Long userId);

    ApiResponse<List<FriendshipResponse>> getSentRequests(Long userId);

    ApiResponse<List<FriendshipResponse>> getFriends(Long userId);

    ApiResponse<String> removeFriend(Long friendshipId, Long userId);
}