package com.jomap.backend.Controllers.Post;

import com.jomap.backend.Services.Community.Posts.PostsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import jakarta.validation.Valid;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostsServices postServices;

    @Autowired
    public PostController(PostsServices postService) {
        this.postServices = postService;
    }

    // GET /api/posts
    @GetMapping
    public List<PostResponse> findAll() {
        return postServices.findAllResponses();
    }

    // GET /api/posts/{postId}
    @GetMapping("/{postId}")
    public ResponseEntity<?> findById(@PathVariable Long postId) {
        return postServices.findResponseById(postId);
    }

    // GET /api/posts/feed/summary
    @GetMapping("/feed/summary")
    public List<PostResponse> feedSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postServices.feedSummary(page, size);
    }

    // POST /api/posts
    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostRequest request) {
        return postServices.createPost(request);
    }

    // PUT /api/posts/{postId}
    @PutMapping("/{postId}")
    public ResponseEntity<?> editPost(@PathVariable Long postId,
                                      @Valid @RequestBody UpdatePostRequest request) {
        return postServices.update(postId, request);
    }

    // DELETE /api/posts/{postId}
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        return postServices.deletePost(postId);
    }
}
