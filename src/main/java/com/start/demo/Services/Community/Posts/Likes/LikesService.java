package com.start.demo.Services.Community.Posts.Likes;

import com.start.demo.Entities.Posts.postLikes.PostLikes;
import org.springframework.http.ResponseEntity;

public interface LikesService {

    Long countByPostId(Long postId);

    ResponseEntity<?> existsByPostId(Long postId);

    ResponseEntity<?> addLike(Long postId);

    ResponseEntity<?> deleteByPostId(Long postId);
}