package com.jomap.backend.DTOs.FriendShips;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendshipResponse {

    private Long id;
    private Long friendshipId;

    private UserFriendDto sender;
    private UserFriendDto receiver;

    // Flat fields for backward compatibility
    private Long senderId;
    private String senderUsername;
    private String senderProfileImage;

    private Long receiverId;
    private String receiverUsername;
    private String receiverProfileImage;

    private Long friendId;
    private String friendUsername;
    private String friendProfileImageUrl;

    private String status;

    private LocalDateTime createdAt;

    @Data
    public static class UserFriendDto {
        private Long id;
        private String username;
        private String profileImageUrl;
        private String email;
        private String firstName;
        private String lastName;
    }
}