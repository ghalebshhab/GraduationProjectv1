package com.jomap.backend.Entities.Posts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    long countByIsDeletedFalse();

    long countByIsDeletedTrue();
    List<Post> findAllByOrderByCreatedAtDesc();
}
