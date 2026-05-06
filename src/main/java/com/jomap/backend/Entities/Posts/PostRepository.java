package com.jomap.backend.Entities.Posts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    long countByIsDeletedFalse();

    long countByIsDeletedTrue();
//
//    Optional<Post> findAllByOrderByCreatedAtDesc();
List<Post> findAll();
}
