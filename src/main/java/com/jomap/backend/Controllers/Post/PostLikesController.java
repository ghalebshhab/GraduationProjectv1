package com.jomap.backend.Controllers.Post;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Likes.PostLikeResponse;
import com.jomap.backend.Services.Community.Posts.Likes.PostLikeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PostLikesController {

    private final PostLikeService postLikeService;

    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<ApiResponse<Long>> likesCount(@PathVariable Long postId) {
        return ResponseEntity.ok(postLikeService.countByPostId(postId));
    }

    @GetMapping("/{postId}/likes/exist")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> existByUserIdAndPostId(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(postLikeService.existsByPostId(emailFromToken, postId));
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<PostLikeResponse>> addLike(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(postLikeService.addLike(emailFromToken, postId));
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<String>> deleteByPostId(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(postLikeService.deleteByPostId(emailFromToken, postId));
    }
}