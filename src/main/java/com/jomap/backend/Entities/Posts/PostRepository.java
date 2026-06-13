package com.jomap.backend.Entities.Posts;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import org.springframework.data.domain.Pageable;

public interface PostRepository extends JpaRepository<Post,Long> {

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.type IN :types ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByTypes(@Param("types") List<Post.PostType> types, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.author.Id = :author_id AND p.isDeleted = false and p.category = :category ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByUserId(@Param("author_id") int userId, @Param("category") String Category);

    @Query("SELECT p FROM Post p WHERE p.author.id = :author_id AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByUserId(@Param("author_id") Long userId);

    @Query("SELECT p FROM Post p WHERE p.author.id = :author_id AND p.isDeleted = false AND p.type = :type ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByUserIdAndType(@Param("author_id") Long userId, @Param("type") Post.PostType type);

    @Query("SELECT p FROM Post p WHERE p.activityId = :activity_id AND (p.isDeleted = false OR p.isDeleted IS NULL) ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByActivityId(@Param("activity_id") Long activityId);

    List<Post> findByAuthorId(Long authorId);

    long countByIsDeletedFalse();

    long countByIsDeletedTrue();

    List<Post> findAll();
}
