package com.jomap.backend.Services.Community.Posts.Comments;

import com.jomap.backend.DTOs.Posts.Comments.PostCommentResponse;
import com.jomap.backend.Entities.Posts.postComments.PostComment;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface CommentsServices {
    Long countByPostId(Long postId);
    ResponseEntity<?> findById(Long commentId);
    List<PostCommentResponse> findByPostId(Long postId);

    ResponseEntity<?> addComment(Long postId, String content);

    ResponseEntity<?> updateComment(Long commentId, String content);

    ResponseEntity<?> deleteComment(Long commentId);
}