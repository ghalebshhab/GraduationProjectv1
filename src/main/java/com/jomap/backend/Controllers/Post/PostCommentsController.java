package com.jomap.backend.Controllers.Post;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.DTOs.Posts.Comments.UpdatePostCommentRequest;
import com.jomap.backend.Services.Community.Posts.Comments.PostCommentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PostCommentsController {

    private final PostCommentService postCommentService;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<List<PostCommentResponse>>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(postCommentService.getCommentsByPostId(postId));
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<PostCommentResponse>> getCommentById(@PathVariable Long commentId) {
        return ResponseEntity.ok(postCommentService.getCommentById(commentId));
    }

    @GetMapping("/{postId}/comments/count")
    public ResponseEntity<ApiResponse<Long>> countByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(postCommentService.countByPostId(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PostCommentResponse>> addComment(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody CreatePostCommentRequest request
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(postCommentService.addComment(emailFromToken, postId, request));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<PostCommentResponse>> updateComment(
            Authentication authentication,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdatePostCommentRequest request
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(postCommentService.updateComment(emailFromToken, commentId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            Authentication authentication,
            @PathVariable Long commentId
    ) {
        String emailFromToken = authentication.getName();
        return ResponseEntity.ok(postCommentService.deleteComment(emailFromToken, commentId));
    }
}