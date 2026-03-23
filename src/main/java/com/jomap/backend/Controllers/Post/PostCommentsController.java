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

//@CrossOrigin(origins = "http://localhost:3000")
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<?> findById(@PathVariable Long commentId) {
        ResponseEntity<?> response = service.findById(commentId);

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        PostComment comment = (PostComment) response.getBody();
        return ResponseEntity.ok(mapToResponse(comment));
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

        ResponseEntity<?> response = service.addComment(postId, request.getContent());

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        PostComment comment = (PostComment) response.getBody();
        return ResponseEntity.ok(mapToResponse(comment));
    }

    // PUT /api/posts/comments/{commentId}
    @PutMapping("/posts/comments/{commentId}")
    public ResponseEntity<?> editComment(@PathVariable Long commentId,
                                         @Valid @RequestBody UpdatePostCommentRequest request) {

        ResponseEntity<?> response = service.updateComment(commentId, request.getContent());

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        PostComment comment = (PostComment) response.getBody();
        return ResponseEntity.ok(mapToResponse(comment));
    }

    // DELETE /api/posts/comments/{commentId}
    @DeleteMapping("/posts/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        return service.deleteComment(commentId);
    }

    private PostCommentResponse mapToResponse(PostComment comment) {
       return null;
//        return new PostCommentResponse(
//                comment.getId(),
//                comment.getPost() != null ? comment.getPost().getId() : null,
//                comment.getUser() != null ? comment.getUser().getId() : null,
//                comment.getUser() != null ? comment.getUser().getUsername() : null,
//                comment.getContent(),
//                comment.getDeleted(),
//                comment.getCreatedAt(),
//                comment.getUpdatedAt()
//        );
    }
}