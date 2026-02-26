package com.start.demo.Services.Posts.Likes;

import com.start.demo.Entities.Posts.postLikes.PostLikes;

public interface LikesService {
    Long countById(Long postId);
    Boolean existsByPostIdAndUserKey(Long postId, String userKey);
    String deleteByPostIdAndUserKey(Long postId,String userKey);
    String addLike(Long postId, String userKey);
}
