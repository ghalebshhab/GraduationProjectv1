package com.jomap.backend.DTOs.FriendShips;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendshipResponse {

    private Long friendshipId;

    private Long senderId;
    private String senderUsername;
    private String senderProfileImage;

    private Long receiverId;
    private String receiverUsername;
    private String receiverProfileImage;

    private String status;

    private LocalDateTime createdAt;
}