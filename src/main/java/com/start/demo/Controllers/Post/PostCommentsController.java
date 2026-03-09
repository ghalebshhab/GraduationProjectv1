package com.start.demo.Controllers.Post;

import com.start.demo.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.start.demo.DTOs.Posts.Comments.PostCommentResponse;
import com.start.demo.DTOs.Posts.Comments.UpdatePostCommentRequest;
import com.start.demo.Entities.Posts.postComments.PostComment;
import com.start.demo.Services.Community.Posts.Comments.CommentsServices;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
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
        return service.findByPostId(postId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // GET /api/posts/comments/{commentId}
    @GetMapping("/posts/comments/{commentId}")
    public PostCommentResponse findById(@PathVariable Long commentId) {
        PostComment comment = service.findById(commentId);
        return mapToResponse(comment);
    }

    // GET /api/posts/{postId}/comments/count
    @GetMapping("/posts/{postId}/comments/count")
    public Long countByPostId(@PathVariable Long postId) {
        return service.countByPostId(postId);
    }

    // POST /api/posts/{postId}/comments
    @PostMapping("/posts/{postId}/comments")
    public PostCommentResponse addComment(@PathVariable Long postId,
                                          @Valid @RequestBody CreatePostCommentRequest request) {

        PostComment comment = service.addComment(postId, request.getContent());
        return mapToResponse(comment);
    }

    // PUT /api/posts/comments/{commentId}
    @PutMapping("/posts/comments/{commentId}")
    public PostCommentResponse editComment(@PathVariable Long commentId,
                                           @Valid @RequestBody UpdatePostCommentRequest request) {

        PostComment comment = service.updateComment(commentId, request.getContent());
        return mapToResponse(comment);
    }

    // DELETE /api/posts/comments/{commentId}
    @DeleteMapping("/posts/comments/{commentId}")
    public String deleteComment(@PathVariable Long commentId) {
        return service.deleteComment(commentId);
    }

    private PostCommentResponse mapToResponse(PostComment comment) {
        return new PostCommentResponse(
                comment.getId(),
                comment.getPost() != null ? comment.getPost().getId() : null,
                comment.getUser() != null ? comment.getUser().getId() : null,
                comment.getUser() != null ? comment.getUser().getUsername() : null,
                comment.getContent(),
                comment.getDeleted(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}