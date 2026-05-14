package com.jomap.backend.Controllers.Friendship;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.FriendShips.FriendshipResponse;
import com.jomap.backend.Entities.Users.User;
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
        String email = authentication.getName();

        ApiResponse<FriendshipResponse> response =
                friendshipService.sendFriendRequest(email, receiverId);

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<ApiResponse<FriendshipResponse>> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long receiverId
    ) {
        ApiResponse<FriendshipResponse> response =
                friendshipService.acceptFriendRequest(friendshipId, receiverId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{friendshipId}/reject")
    public ResponseEntity<ApiResponse<String>> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long receiverId
    ) {
        ApiResponse<String> response =
                friendshipService.rejectFriendRequest(friendshipId, receiverId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{friendshipId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long senderId
    ) {
        ApiResponse<String> response =
                friendshipService.cancelFriendRequest(friendshipId, senderId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending/{userId}")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> getPendingRequests(
            @PathVariable Long userId
    ) {
        ApiResponse<List<FriendshipResponse>> response =
                friendshipService.getPendingRequests(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> getSentRequests(
            @PathVariable Long userId
    ) {
        ApiResponse<List<FriendshipResponse>> response =
                friendshipService.getSentRequests(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<ApiResponse<List<FriendshipResponse>>> getFriends(
            @PathVariable Long userId
    ) {
        ApiResponse<List<FriendshipResponse>> response =
                friendshipService.getFriends(userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{friendshipId}/remove")
    public ResponseEntity<ApiResponse<String>> removeFriend(
            @PathVariable Long friendshipId,
            @RequestParam Long userId
    ) {
        ApiResponse<String> response =
                friendshipService.removeFriend(friendshipId, userId);

        return ResponseEntity.ok(response);
    }
}