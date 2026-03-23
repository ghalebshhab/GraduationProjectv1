package com.jomap.backend.Services.Community.Posts.Likes;

import org.springframework.http.ResponseEntity;

public interface LikesService {

    Long countByPostId(Long postId);

    ResponseEntity<?> existsByPostId(Long postId);

    ResponseEntity<?> addLike(Long postId);

    ResponseEntity<?> deleteByPostId(Long postId);
}