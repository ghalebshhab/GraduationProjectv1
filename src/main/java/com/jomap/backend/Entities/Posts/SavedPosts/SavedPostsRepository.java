package com.jomap.backend.Entities.Posts.SavedPosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedPostsRepository extends JpaRepository<SavedPost, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<SavedPost> findByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);

    List<SavedPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);
}