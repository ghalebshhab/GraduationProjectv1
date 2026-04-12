package com.jomap.backend.Entities.Posts.postLikes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikesRepository extends JpaRepository<PostLikes, Long> {

    // ── Count all likes for a single post ─────────────────────────────────────
    long countByPostId(Long postId);

    // ── Duplicate-like guard ───────────────────────────────────────────────────
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // ── Find a specific like record (for delete and addLike idempotency) ──────
    Optional<PostLikes> findByPostIdAndUserId(Long postId, Long userId);

    // ── Feed algorithm: all post IDs liked by a given user ────────────────────
    @Query("SELECT pl.post.id FROM PostLikes pl WHERE pl.user.id = :userId")
    List<Long> findPostIdsLikedByUser(@Param("userId") Long userId);

    // ── Feed algorithm: bulk like counts (avoids N+1) ─────────────────────────
    // Returns Object[]{ postId, count } — converted to Map in the service
    @Query("SELECT pl.post.id, COUNT(pl) FROM PostLikes pl " +
            "WHERE pl.post.id IN :postIds GROUP BY pl.post.id")
    List<Object[]> countLikesByPostIds(@Param("postIds") List<Long> postIds);
}