package com.jomap.backend.Entities.Posts;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post,Long> {

    @Query("SELECT p FROM Post p WHERE p.author.Id = :author_id AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByUserId(@Param("author_id") int userId);

    long countByIsDeletedFalse();

    long countByIsDeletedTrue();

    List<Post> findAll();
}
