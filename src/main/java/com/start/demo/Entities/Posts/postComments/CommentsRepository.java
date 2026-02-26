package com.start.demo.Entities.Posts.postComments;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<PostComment,Long> {
}
