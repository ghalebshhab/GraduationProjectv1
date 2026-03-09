package com.start.demo.Services.Community.Posts.Comments;

import com.start.demo.Entities.Posts.postComments.PostComment;

import java.util.List;

import com.start.demo.Entities.Posts.postComments.PostComment;
import java.util.List;

public interface CommentsServices {
    Long countByPostId(Long postId);
    PostComment findById(Long commentId);
    List<PostComment> findByPostId(Long postId);

    PostComment addComment(Long postId, String content);

    PostComment updateComment(Long commentId, String content);

    String deleteComment(Long commentId);
}