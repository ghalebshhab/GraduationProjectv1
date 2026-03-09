package com.start.demo.Services.Community.Posts.Likes;

import com.start.demo.Entities.Posts.postLikes.PostLikes;

public interface LikesService {

    // عدد اللايكات على بوست
    Long countByPostId(Long postId);

    // هل هذا المستخدم عمل لايك على هذا البوست؟
    Boolean existsByPostId(Long postId);

    // إضافة لايك
    PostLikes addLike(Long postId);

    // حذف لايك
    String deleteByPostId(Long postId);
}