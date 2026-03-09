package com.start.demo.Controllers.Post;

import com.start.demo.DTOs.Posts.Likes.CreatePostLikeRequest;
import com.start.demo.DTOs.Posts.Likes.PostLikeResponse;
import com.start.demo.Services.Community.Posts.Likes.LikesService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/posts")
public class PostLikesController {

    private final LikesService likes;

    public PostLikesController(LikesService likes) {
        this.likes = likes;
    }

    // GET /api/posts/{postId}/likes/count
    @GetMapping("/{postId}/likes/count")
    public Long likesCount(@PathVariable Long postId) {
        return likes.countByPostId(postId);
    }

    // POST /api/posts/{postId}/likes
    @PostMapping("/{postId}/likes")
    public PostLikeResponse addLike(@PathVariable Long postId,
                                    @Valid @RequestBody CreatePostLikeRequest request) {

        var like = likes.addLike(postId);

        return new PostLikeResponse(
                like.getId(),
                like.getPost().getId(),
                like.getUser().getId(),
                like.getUser().getUsername(),
                like.getCreatedAt()
        );

    }


    // GET /api/posts/{postId}/likes/exist?userId=1
    @GetMapping("/{postId}/likes/exist")
    public Boolean existByUserIdAndPostId(@PathVariable Long postId,
                                          @RequestParam Long userId) {
        return likes.existsByPostId(postId);
    }

    // DELETE /api/posts/{postId}/likes
    @DeleteMapping("/{postId}/likes")
    public String deleteByPostId(@PathVariable Long postId,
                                          @Valid @RequestBody CreatePostLikeRequest request) {

        return likes.deleteByPostId(postId);
    }
}