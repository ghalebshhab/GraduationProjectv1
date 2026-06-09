package com.jomap.backend.Services.Friendship;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.FriendShips.FriendshipResponse;

import java.util.List;

public interface FriendshipService {

    ApiResponse<FriendshipResponse> sendFriendRequest(String senderEmail, Long receiverId);

    ApiResponse<FriendshipResponse> acceptFriendRequest(Long friendshipId, String receiverEmail);

    ApiResponse<String> rejectFriendRequest(Long friendshipId, String receiverEmail);

    ApiResponse<String> cancelFriendRequest(Long friendshipId, String senderEmail);

    ApiResponse<List<FriendshipResponse>> getPendingRequests(String receiverEmail);

    ApiResponse<List<FriendshipResponse>> getSentRequests(String senderEmail);

    ApiResponse<List<FriendshipResponse>> getFriends(String userEmail);

    ApiResponse<String> removeFriend(Long friendshipId, String userEmail);

    ApiResponse<FriendshipResponse> checkFriendshipStatus(String currentUserEmail, Long targetUserId);
}