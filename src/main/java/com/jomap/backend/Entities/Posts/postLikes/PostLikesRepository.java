package com.jomap.backend.Entities.Posts.postLikes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikesRepository extends JpaRepository<PostLikes, Long> {
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    Optional<PostLikes> findByPostIdAndUserId(Long postId, Long userId);
}