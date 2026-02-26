package com.start.demo.Services.Posts.Comments;

import com.start.demo.Entities.Posts.postComments.PostComment;

import java.util.List;

public interface CommentsServices {
    Long countByPostId(Long postId);

    PostComment findById(Long commentId);

    List<PostComment> findByPostId(Long postId);

    PostComment addComment(PostComment comment);

    PostComment updateComment(Long commentId, PostComment updated);

    String deleteComment(Long commentId);
}
