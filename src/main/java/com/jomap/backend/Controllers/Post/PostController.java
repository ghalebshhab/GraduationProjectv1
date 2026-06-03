package com.jomap.backend.Controllers.Post;

import java.util.List;
import java.util.Locale.Category;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.PostCategory;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Services.Community.Posts.PostsServices;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PostController {

    private final PostsServices postService;

    @GetMapping("getpostsuser/{userId}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts(@PathVariable int userId, @Valid @RequestBody PostCategory request) {
        return ResponseEntity.ok(postService.getAllPosts(userId, request.category));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

    @GetMapping("/feed/summary")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getFeedSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return ResponseEntity.ok(postService.getFeedSummary(page, size));
    }

    @GetMapping("/feed/personalized")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPersonalizedFeed(
            Authentication authentication,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.getPersonalizedFeed(authentication.getName(), lat, lng, page, size));
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

    @GetMapping("/my-posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getMyPosts(Authentication authentication) {
        return ResponseEntity.ok(postService.getMyPosts(authentication.getName()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getUserPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }
}