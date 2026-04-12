package com.jomap.backend.Entities.Posts.postComments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("""
        select pc
        from PostComment pc
        where pc.post.id = :postId
          and pc.isDeleted = false
        order by pc.createdAt desc
    """)
    List<PostComment> findActiveByPostId(@Param("postId") Long postId);

    @Query("""
        select count(pc)
        from PostComment pc
        where pc.post.id = :postId
          and pc.isDeleted = false
    """)
    long countActiveByPostId(@Param("postId") Long postId);

    @Query("""
        select distinct pc.post.id
        from PostComment pc
        where pc.author.id = :userId
          and pc.isDeleted = false
    """)
    List<Long> findPostIdsCommentedByUser(@Param("userId") Long userId);

    @Query("""
        select pc.post.id, count(pc)
        from PostComment pc
        where pc.post.id in :postIds
          and pc.isDeleted = false
        group by pc.post.id
    """)
    List<Object[]> countCommentsByPostIds(@Param("postIds") List<Long> postIds);
}