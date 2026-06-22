package com.jomap.backend.Controllers.Users;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.BlockedLocationResponse;
import com.jomap.backend.DTOs.Users.BlockedUserResponse;
import com.jomap.backend.Services.Users.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "https://jomab-712232187160.europe-west1.run.app", "https://jomap-admin.web.app", "https://jomap-admin.firebaseapp.com"})
public class UserBlockController {

    private final UserBlockService userBlockService;

    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<BlockedUserResponse>>> getBlockedUsers(
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(userBlockService.getBlockedUsers(email));
    }

    @PostMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<String>> blockUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(userBlockService.blockUser(email, userId));
    }

    @DeleteMapping("/{userId}/unblock")
    public ResponseEntity<ApiResponse<String>> unblockUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(userBlockService.unblockUser(email, userId));
    }

    @GetMapping("/blocks/locations")
    public ResponseEntity<ApiResponse<List<BlockedLocationResponse>>> getBlockedLocations(
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(userBlockService.getBlockedLocations(email));
    }
}
