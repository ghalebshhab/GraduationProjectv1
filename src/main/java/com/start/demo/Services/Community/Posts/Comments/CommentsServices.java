package com.start.demo.Services.Community.Posts.Comments;

import com.start.demo.Entities.Posts.postComments.PostComment;

import java.util.List;

import com.start.demo.Entities.Posts.postComments.PostComment;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CommentsServices {
    Long countByPostId(Long postId);
    ResponseEntity<?> findById(Long commentId);
    List<PostComment> findByPostId(Long postId);

    ResponseEntity<?> addComment(Long postId, String content);

    ResponseEntity<?> updateComment(Long commentId, String content);

    ResponseEntity<?> deleteComment(Long commentId);
}