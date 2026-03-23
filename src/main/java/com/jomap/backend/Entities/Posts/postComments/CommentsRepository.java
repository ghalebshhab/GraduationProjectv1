package com.jomap.backend.Entities.Posts.postComments;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(Long postId);
    long countByPostIdAndIsDeletedFalse(Long postId);
}