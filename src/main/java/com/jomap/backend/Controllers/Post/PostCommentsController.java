package com.jomap.backend.Controllers.Post;

import com.jomap.backend.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.DTOs.Posts.Comments.UpdatePostCommentRequest;
import com.jomap.backend.Entities.Posts.postComments.PostComment;
import com.jomap.backend.Services.Community.Posts.Comments.CommentsServices;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RestController
@RequestMapping("/api")
public class PostCommentsController {

    private final CommentsServices service;

    public PostCommentsController(CommentsServices service) {
        this.service = service;
    }

    // GET /api/posts/{postId}/comments
    @GetMapping("/posts/{postId}/comments")
    public List<PostCommentResponse> findAll(@PathVariable Long postId) {
        return service.findByPostId(postId);
    }

    // GET /api/posts/comments/{commentId}
    @GetMapping("/posts/comments/{commentId}")
    public ResponseEntity<?> findById(@PathVariable Long commentId) {
        return service.findById(commentId);
    }

    // GET /api/posts/{postId}/comments/count
    @GetMapping("/posts/{postId}/comments/count")
    public Long countByPostId(@PathVariable Long postId) {
        return service.countByPostId(postId);
    }

    // POST /api/posts/{postId}/comments
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @Valid @RequestBody CreatePostCommentRequest request) {
        return service.addComment(postId, request.getContent());
    }

    // PUT /api/posts/comments/{commentId}
    @PutMapping("/posts/comments/{commentId}")
    public ResponseEntity<?> editComment(@PathVariable Long commentId,
                                         @Valid @RequestBody UpdatePostCommentRequest request) {
        return service.updateComment(commentId, request.getContent());
    }

    // DELETE /api/posts/comments/{commentId}
    @DeleteMapping("/posts/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        return service.deleteComment(commentId);
    }
}