package com.jomap.backend.Controllers.Post;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Services.Community.Posts.PostsServices;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PostController {

    private final PostsServices postService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/feed/summary")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getFeedSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.getFeedSummary(page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            Authentication authentication,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return ResponseEntity.ok(postService.createPost(authentication.getName(), request));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return ResponseEntity.ok(postService.updatePost(authentication.getName(), postId, request));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> deletePost(
            Authentication authentication,
            @PathVariable Long postId
    ) {
        return ResponseEntity.ok(postService.deletePost(authentication.getName(), postId));
    }
}