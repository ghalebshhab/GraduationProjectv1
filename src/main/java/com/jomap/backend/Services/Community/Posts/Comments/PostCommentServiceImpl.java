package com.jomap.backend.Services.Community.Posts.Comments;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Comments.CreatePostCommentRequest;
import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.DTOs.Posts.Comments.UpdatePostCommentRequest;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Posts.postComments.PostComment;
import com.jomap.backend.Entities.Posts.postComments.PostCommentRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<PostCommentResponse>> getCommentsByPostId(Long postId) {
        List<PostCommentResponse> responses = postCommentRepository
                .findActiveByPostId(postId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ApiResponse.success("Comments fetched successfully", responses);
    }

    @Override
    public ApiResponse<PostCommentResponse> getCommentById(Long commentId) {
        PostComment comment = postCommentRepository.findById(commentId).orElse(null);

        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            return ApiResponse.error("Comment not found");
        }

        return ApiResponse.success("Comment fetched successfully", toResponse(comment));
    }

    @Override
    public ApiResponse<Long> countByPostId(Long postId) {
        long count = postCommentRepository.countActiveByPostId(postId);
        return ApiResponse.success("Comment count fetched", count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PostCommentResponse> addComment(String emailFromToken,
                                                       Long postId,
                                                       CreatePostCommentRequest request) {
        try {
            System.out.println("=== addComment START ===");
            System.out.println("emailFromToken = " + emailFromToken);
            System.out.println("postId = " + postId);
            System.out.println("request = " + request);
            System.out.println("content = " + (request != null ? request.getContent() : null));

            if (emailFromToken == null || emailFromToken.isBlank()) {
                System.out.println("FAILED: emailFromToken is null or blank");
                return ApiResponse.error("Unauthorized user");
            }

            if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
                System.out.println("FAILED: comment content is empty");
                return ApiResponse.error("Comment content is required");
            }

            User user = userRepository.findByEmail(emailFromToken).orElse(null);
            System.out.println("user found = " + (user != null ? user.getId() : null));

            if (user == null) {
                System.out.println("FAILED: user not found");
                return ApiResponse.error("User not found");
            }

            Post post = postRepository.findById(postId).orElse(null);
            System.out.println("post found = " + (post != null ? post.getId() : null));
            System.out.println("post deleted = " + (post != null ? post.getIsDeleted() : null));

            if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
                System.out.println("FAILED: post not found or deleted");
                return ApiResponse.error("Post not found");
            }

            PostComment comment = new PostComment();
            comment.setPost(post);
            comment.setAuthor(user);
            comment.setContent(request.getContent().trim());
            comment.setIsDeleted(false);

            System.out.println("Before saveAndFlush...");
            PostComment saved = postCommentRepository.saveAndFlush(comment);
            System.out.println("After saveAndFlush, saved id = " + saved.getId());

            return ApiResponse.success("Comment added successfully", toResponse(saved));

        } catch (Exception e) {
            System.out.println("EXCEPTION IN addComment: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("Failed to add comment: " + e.getMessage());
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<PostCommentResponse> updateComment(String emailFromToken,
                                                          Long commentId,
                                                          UpdatePostCommentRequest request) {
        try {
            if (emailFromToken == null || emailFromToken.isBlank()) {
                return ApiResponse.error("Unauthorized user");
            }

            if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ApiResponse.error("Comment content is required");
            }

            User user = userRepository.findByEmail(emailFromToken).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found");
            }

            PostComment comment = postCommentRepository.findById(commentId).orElse(null);
            if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
                return ApiResponse.error("Comment not found");
            }

            if (!comment.getAuthor().getId().equals(user.getId())) {
                return ApiResponse.error("You can only edit your own comments");
            }

            comment.setContent(request.getContent().trim());

            PostComment saved = postCommentRepository.saveAndFlush(comment);

            return ApiResponse.success("Comment updated successfully", toResponse(saved));

        } catch (DataAccessException e) {
            return ApiResponse.error("Database error while updating comment: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Failed to update comment: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> deleteComment(String emailFromToken, Long commentId) {
        try {
            if (emailFromToken == null || emailFromToken.isBlank()) {
                return ApiResponse.error("Unauthorized user");
            }

            User user = userRepository.findByEmail(emailFromToken).orElse(null);
            if (user == null) {
                return ApiResponse.error("User not found");
            }

            PostComment comment = postCommentRepository.findById(commentId).orElse(null);
            if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
                return ApiResponse.error("Comment not found");
            }

            if (!comment.getAuthor().getId().equals(user.getId())) {
                return ApiResponse.error("You can only delete your own comments");
            }

            comment.setIsDeleted(true);
            postCommentRepository.saveAndFlush(comment);

            return ApiResponse.success("Comment deleted successfully", "Comment deleted successfully");

        } catch (DataAccessException e) {
            return ApiResponse.error("Database error while deleting comment: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Failed to delete comment: " + e.getMessage());
        }
    }

    @Override
    public List<Long> getPostIdsCommentedByUser(Long userId) {
        List<Long> ids = postCommentRepository.findPostIdsCommentedByUser(userId);
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return postCommentRepository.countCommentsByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private PostCommentResponse toResponse(PostComment comment) {
        PostCommentResponse r = new PostCommentResponse();
        r.setId(comment.getId());
        r.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
        r.setContent(comment.getContent());
        r.setCreatedAt(comment.getCreatedAt());
        r.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getAuthor() != null) {
            r.setAuthorId(comment.getAuthor().getId());
            r.setAuthorUsername(comment.getAuthor().getUsername());
            r.setAuthorEmail(comment.getAuthor().getEmail());
            r.setAuthorProfileImageUrl(comment.getAuthor().getProfileImageUrl());
        }

        return r;
    }
}