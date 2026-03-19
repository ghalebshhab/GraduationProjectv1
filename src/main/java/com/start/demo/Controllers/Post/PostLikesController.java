package com.start.demo.Controllers.Post;

import com.start.demo.DTOs.Posts.Likes.PostLikeResponse;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Services.Community.Posts.Likes.LikesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/posts")
public class PostLikesController {

    private final LikesService likes;

    public PostLikesController(LikesService likes) {
        this.likes = likes;
    }

    @GetMapping("/{postId}/likes/count")
    public Long likesCount(@PathVariable Long postId) {
        return likes.countByPostId(postId);
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<?> addLike(@PathVariable Long postId) {
        ResponseEntity<?> response = likes.addLike(postId);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        PostLikes like = (PostLikes) response.getBody();

        return ResponseEntity.ok(
                new PostLikeResponse(
                        like.getId(),
                        like.getPost().getId(),
                        like.getUser().getId(),
                        like.getUser().getUsername(),
                        like.getCreatedAt()
                )
        );
    }

    @GetMapping("/{postId}/likes/exist")
    public ResponseEntity<?> existByUserIdAndPostId(@PathVariable Long postId) {
        return likes.existsByPostId(postId);
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<?> deleteByPostId(@PathVariable Long postId) {
        return likes.deleteByPostId(postId);
    }
}