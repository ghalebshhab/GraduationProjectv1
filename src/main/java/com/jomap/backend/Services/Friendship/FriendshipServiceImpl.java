package com.jomap.backend.Services.Friendship;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.FriendShips.FriendshipResponse;
import com.jomap.backend.Entities.Friendship.FriendshipStatus;
import com.jomap.backend.Entities.Friendship.Friendship;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Friendship.FriendshipRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Friendship.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<FriendshipResponse> sendFriendRequest(String senderEmail, Long receiverId) {

        User sender = userRepository.findByEmail(senderEmail).orElse(null);
        if (sender == null) {
            return new ApiResponse<>(false, "Sender user not found", null);
        }

        User receiver = userRepository.findById(receiverId).orElse(null);
        if (receiver == null) {
            return new ApiResponse<>(false, "Receiver user not found", null);
        }

        if (sender.getId().equals(receiver.getId())) {
            return new ApiResponse<>(false, "You cannot send friend request to yourself", null);
        }

        var existingFriendship = friendshipRepository
                .findByRequesterAndReceiverOrRequesterAndReceiver(
                        sender, receiver,
                        receiver, sender
                );

        if (!existingFriendship.isEmpty()) {
            Friendship friendship = existingFriendship.get(existingFriendship.size() - 1);

            if (friendship.getStatus() == FriendshipStatus.PENDING) {
                return new ApiResponse<>(false, "Friend request already exists", mapToResponse(friendship));
            }

            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                return new ApiResponse<>(false, "You are already friends", mapToResponse(friendship));
            }

            if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
                return new ApiResponse<>(false, "Friend request is blocked", null);
            }

            friendship.setRequester(sender);
            friendship.setReceiver(receiver);
            friendship.setStatus(FriendshipStatus.PENDING);

            Friendship updated = friendshipRepository.save(friendship);
            return new ApiResponse<>(true, "Friend request sent again", mapToResponse(updated));
        }

        Friendship friendship = new Friendship();
        friendship.setRequester(sender);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        Friendship saved = friendshipRepository.save(friendship);

        return new ApiResponse<>(true, "Friend request sent successfully", mapToResponse(saved));
    }
    @Override
    @Transactional
    public ApiResponse<FriendshipResponse> acceptFriendRequest(Long friendshipId, String receiverEmail) {

        User receiver = userRepository.findByEmail(receiverEmail).orElse(null);
        if (receiver == null) {
            return new ApiResponse<>(false, "Receiver user not found", null);
        }

        Friendship friendship = friendshipRepository.findById(friendshipId).orElse(null);
        if (friendship == null) {
            return new ApiResponse<>(false, "Friend request not found", null);
        }

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            return new ApiResponse<>(false, "You are not allowed to accept this request", null);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return new ApiResponse<>(false, "Friend request is not pending", mapToResponse(friendship));
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        Friendship saved = friendshipRepository.save(friendship);

        return new ApiResponse<>(true, "Friend request accepted", mapToResponse(saved));
    }
    @Override
    @Transactional
    public ApiResponse<String> rejectFriendRequest(Long friendshipId, String receiverEmail) {

        User receiver = userRepository.findByEmail(receiverEmail).orElse(null);
        if (receiver == null) {
            return new ApiResponse<>(false, "Receiver user not found", null);
        }

        Friendship friendship = friendshipRepository.findById(friendshipId).orElse(null);
        if (friendship == null) {
            return new ApiResponse<>(false, "Friend request not found", null);
        }

        if (!friendship.getReceiver().getId().equals(receiver.getId())) {
            return new ApiResponse<>(false, "You are not allowed to reject this request", null);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return new ApiResponse<>(false, "Friend request is not pending", null);
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);

        return new ApiResponse<>(true, "Friend request rejected", null);
    }
    @Override
    @Transactional
    public ApiResponse<String> cancelFriendRequest(Long friendshipId, String senderEmail) {

        User sender = userRepository.findByEmail(senderEmail).orElse(null);
        if (sender == null) {
            return new ApiResponse<>(false, "Sender user not found", null);
        }

        Friendship friendship = friendshipRepository.findById(friendshipId).orElse(null);
        if (friendship == null) {
            return new ApiResponse<>(false, "Friend request not found", null);
        }

        if (!friendship.getRequester().getId().equals(sender.getId())) {
            return new ApiResponse<>(false, "You are not allowed to cancel this request", null);
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            return new ApiResponse<>(false, "Only pending requests can be cancelled", null);
        }

        friendshipRepository.delete(friendship);

        return new ApiResponse<>(true, "Friend request cancelled", null);
    }
    @Override
    public ApiResponse<List<FriendshipResponse>> getPendingRequests(String receiverEmail) {

        User user = userRepository.findByEmail(receiverEmail).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        List<FriendshipResponse> responses = friendshipRepository
                .findByReceiverAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new ApiResponse<>(true, "Pending friend requests fetched successfully", responses);
    }
    @Override
    public ApiResponse<List<FriendshipResponse>> getSentRequests(String senderEmail) {

        User user = userRepository.findByEmail(senderEmail).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        List<FriendshipResponse> responses = friendshipRepository
                .findByRequesterAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new ApiResponse<>(true, "Sent friend requests fetched successfully", responses);
    }
    @Override
    public ApiResponse<List<FriendshipResponse>> getFriends(String userEmail) {

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        List<FriendshipResponse> responses = friendshipRepository
                .findByRequesterAndStatusOrReceiverAndStatus(
                        user, FriendshipStatus.ACCEPTED,
                        user, FriendshipStatus.ACCEPTED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();

        return new ApiResponse<>(true, "Friends fetched successfully", responses);
    }
    @Override
    @Transactional
    public ApiResponse<String> removeFriend(Long friendshipId, String userEmail) {

        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "User not found", null);
        }

        Friendship friendship = friendshipRepository.findById(friendshipId).orElse(null);
        if (friendship == null) {
            return new ApiResponse<>(false, "Friendship not found", null);
        }

        boolean isSender = friendship.getRequester().getId().equals(user.getId());
        boolean isReceiver = friendship.getReceiver().getId().equals(user.getId());

        if (!isSender && !isReceiver) {
            return new ApiResponse<>(false, "You are not allowed to remove this friendship", null);
        }

        if (friendship.getStatus() != FriendshipStatus.ACCEPTED) {
            return new ApiResponse<>(false, "This friendship is not accepted", null);
        }

        friendshipRepository.delete(friendship);

        return new ApiResponse<>(true, "Friend removed successfully", null);
    }
    private FriendshipResponse mapToResponse(Friendship friendship) {

        FriendshipResponse response = new FriendshipResponse();

        response.setFriendshipId(friendship.getId());

        response.setSenderId(friendship.getRequester().getId());
        response.setSenderUsername(friendship.getRequester().getUsername());

        response.setReceiverId(friendship.getReceiver().getId());
        response.setReceiverUsername(friendship.getReceiver().getUsername());

        // إذا عندك profile image داخل User
        response.setSenderProfileImage(friendship.getRequester().getProfileImageUrl());
        response.setReceiverProfileImage(friendship.getReceiver().getProfileImageUrl());

        response.setStatus(friendship.getStatus().name());
        response.setCreatedAt(friendship.getCreatedAt());

        return response;
    }
}