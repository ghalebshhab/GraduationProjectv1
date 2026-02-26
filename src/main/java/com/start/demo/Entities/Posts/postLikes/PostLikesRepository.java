package com.start.demo.Entities.Posts.postLikes;

import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PostLikesRepository extends JpaRepository<PostLikes,Long> {
}
