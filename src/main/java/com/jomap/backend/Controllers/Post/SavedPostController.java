package com.jomap.backend.Controllers.Post;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.Services.Community.Posts.SavedPosts.SavedPostService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/saved")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SavedPostController {

    private final SavedPostService savedPostService;

    @PostMapping("/{postId}/toggle")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleSave(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(savedPostService.toggleSave(postId, emailFromToken));
    }

    @GetMapping("/status/{postId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isPostSaved(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(savedPostService.isPostSaved(postId, emailFromToken));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMySavedPosts(Authentication authentication) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(savedPostService.getMySavedPosts(emailFromToken));
    }
}