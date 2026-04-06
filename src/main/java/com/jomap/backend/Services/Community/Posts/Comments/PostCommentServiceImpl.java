package com.jomap.backend.Services.Community.Posts.Comments;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.DTOs.Posts.Comments.UpdatePostCommentRequest;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.postComments.PostComment;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entity;

    @Override
    @Transactional
    public ApiResponse<Long> countByPostId(Long postId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(c) FROM PostComment c " +
                        "WHERE c.post.id = :postId AND c.isDeleted = false",
                Long.class
        );
        q.setParameter("postId", postId);

        return ApiResponse.success("Comments count fetched successfully", q.getSingleResult());
    }

    @Override
    @Transactional
    public ApiResponse<PostCommentResponse> getCommentById(Long commentId) {
        PostComment comment = entity.find(PostComment.class, commentId);

        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            return ApiResponse.error("Comment not found");
        }

        return ApiResponse.success("Comment fetched successfully", toResponse(comment));
    }

    @Override
    @Transactional
    public ApiResponse<List<PostCommentResponse>> getCommentsByPostId(Long postId) {
        TypedQuery<PostComment> q = entity.createQuery(
                "FROM PostComment c " +
                        "WHERE c.post.id = :postId AND c.isDeleted = false " +
                        "ORDER BY c.createdAt DESC",
                PostComment.class
        );
        q.setParameter("postId", postId);

        List<PostCommentResponse> responses = q.getResultList()
                .stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success("Comments fetched successfully", responses);
    }

    @Override
    @Transactional
    public ApiResponse<PostCommentResponse> addComment(String emailFromToken, Long postId, CreatePostCommentRequest request) {
        User user = userRepository.findByEmail(emailFromToken).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.error("Comment cannot be empty");
        }

        Post post = entity.find(Post.class, postId);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.getContent().trim());
        comment.setIsDeleted(false);

        entity.persist(comment);
        entity.flush();

        return ApiResponse.success("Comment added successfully", toResponse(comment));
    }

    @Override
    @Transactional
    public ApiResponse<PostCommentResponse> updateComment(String emailFromToken, Long commentId, UpdatePostCommentRequest request) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        PostComment existing = entity.find(PostComment.class, commentId);
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            return ApiResponse.error("Comment not found");
        }

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only edit your own comment");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.error("Content is required");
        }

        existing.setContent(request.getContent().trim());
        entity.merge(existing);
        entity.flush();

        return ApiResponse.success("Comment updated successfully", toResponse(existing));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteComment(String emailFromToken, Long commentId) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        PostComment existing = entity.find(PostComment.class, commentId);
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            return ApiResponse.error("Comment not found");
        }

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only delete your own comment");
        }

        existing.setIsDeleted(true);
        entity.merge(existing);
        entity.flush();

        return ApiResponse.success("Comment deleted successfully", "Comment deleted successfully");
    }

    private PostCommentResponse toResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();

        response.setId(comment.getId());
        response.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);

        if (comment.getUser() != null) {
            response.setUserId(comment.getUser().getId());
            response.setUsername(comment.getUser().getUsername());
            response.setUserProfileImageUrl(comment.getUser().getProfileImageUrl());
        }

        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());

        return response;
    }
}