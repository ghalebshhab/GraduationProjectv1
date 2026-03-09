package com.start.demo.Controllers.Post;
import com.start.demo.Services.Community.Posts.PostsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.start.demo.DTOs.Posts.CreatePostRequest;
import com.start.demo.DTOs.Posts.PostResponse;
import com.start.demo.DTOs.Posts.UpdatePostRequest;
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

    // ✅ Get all (اختياري: عادةً تستخدم feed بدلها)
    @GetMapping
    public List<PostResponse> findAll() {
        return postServices.findAllResponses();
    }

    // ✅ Get one post
    @GetMapping("/{postId}")
    public PostResponse findById(@PathVariable Long postId) {
        return postServices.findResponseById(postId);
    }

    // ✅ Feed summary (post + counts)
    @GetMapping("/feed/summary")
    public List<PostResponse> feedSummary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return postServices.feedSummary(page, size);
    }

    // ✅ Create post (authorId داخل ال DTO)
    @PostMapping
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return postServices.create(request);
    }

    // ✅ Update post (بدون تغيير author)
    @PutMapping("/{postId}")
    public PostResponse editPost(@PathVariable Long postId,
                                 @Valid @RequestBody UpdatePostRequest request) {
        return postServices.update(postId, request);
    }

    @DeleteMapping("/{postId}")
    public String deletePost(@PathVariable Long postId) {
        postServices.deletePost(postId);
        return "The post with the id -" + postId + " is deleted";
    }
}
