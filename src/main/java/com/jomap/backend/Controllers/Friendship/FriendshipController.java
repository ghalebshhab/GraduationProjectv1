package com.jomap.backend.Controllers.Friendship;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.FriendShips.FriendshipResponse;
import com.jomap.backend.Services.Friendship.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friendships")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<FriendshipResponse>> sendFriendRequest(
            @RequestParam Long receiverId,
            Authentication authentication
    ) {
        String senderEmail = authentication.getName();

        ApiResponse<FriendshipResponse> response =
                friendshipService.sendFriendRequest(senderEmail, receiverId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<ApiResponse<FriendshipResponse>> acceptFriendRequest(
            @PathVariable Long friendshipId,
            Authentication authentication
    ) {
        String receiverEmail = authentication.getName();

        ApiResponse<FriendshipResponse> response =
                friendshipService.acceptFriendRequest(friendshipId, receiverEmail);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectFriendRequest(
            @PathVariable Long friendshipId,
            Authentication authentication
    ) {
        String receiverEmail = authentication.getName();

        ApiResponse<String> response =
                friendshipService.rejectFriendRequest(friendshipId, receiverEmail);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{friendshipId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelFriendRequest(
            @PathVariable Long friendshipId,
            Authentication authentication
    ) {
        String senderEmail = authentication.getName();

        ApiResponse<String> response =
                friendshipService.cancelFriendRequest(friendshipId, senderEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> getPendingRequests(
            Authentication authentication
    ) {
        String receiverEmail = authentication.getName();

        ApiResponse<List<FriendshipResponse>> response =
                friendshipService.getPendingRequests(receiverEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> getSentRequests(
            Authentication authentication
    ) {
        String senderEmail = authentication.getName();

        ApiResponse<List<FriendshipResponse>> response =
                friendshipService.getSentRequests(senderEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/friends")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> getFriends(
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        ApiResponse<List<FriendshipResponse>> response =
                friendshipService.getFriends(userEmail);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{friendshipId}/remove")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @PathVariable Long friendshipId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();

        ApiResponse<String> response =
                friendshipService.removeFriend(friendshipId, userEmail);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<ApiResponse<FriendshipResponse>> checkFriendshipStatus(
            @PathVariable Long targetUserId,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ApiResponse<FriendshipResponse> response =
                friendshipService.checkFriendshipStatus(userEmail, targetUserId);
        return ResponseEntity.ok(response);
    }
}