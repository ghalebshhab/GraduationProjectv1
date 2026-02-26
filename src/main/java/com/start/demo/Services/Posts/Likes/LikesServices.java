package com.start.demo.Services.Posts.Likes;

import com.start.demo.Entities.Posts.Post;
import com.start.demo.Entities.Posts.postLikes.PostLikes;
import com.start.demo.Entities.Posts.postLikes.PostLikesRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.swing.text.StyledEditorKit;
import java.util.Optional;

@Service
public class LikesServices implements LikesService{
    PostLikesRepository postLikes;
    EntityManager entity;
    public LikesServices(PostLikesRepository postLikes,EntityManager entity){
        this.postLikes=postLikes;
        this.entity=entity;
    }
    @Override
    public Long countById(Long postId) {

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(pl) FROM PostLikes pl WHERE pl.postId = :postId",
                Long.class
        );

        query.setParameter("postId", postId);

        return query.getSingleResult(); // returns 0 if no likes
    }

    @Override
    public Boolean existsByPostIdAndUserKey(Long postId, String userKey) {

        TypedQuery<Long> query = entity.createQuery(
                "SELECT COUNT(pl) FROM PostLikes pl WHERE pl.postId = :postId AND pl.userKey = :userKey",
                Long.class
        );

        query.setParameter("postId", postId);
        query.setParameter("userKey", userKey);

        return query.getSingleResult() > 0;
    }

    @Override
    public String deleteByPostIdAndUserKey(Long postId, String userKey) {
    TypedQuery<PostLikes> theQuery= entity.createQuery("FROM PostLikes WHERE postId=:postId AND userKey=:userKey ",PostLikes.class);
        theQuery.setParameter("postId",postId);
        theQuery.setParameter("userKey",userKey);
        Long id=theQuery.getSingleResult().getId();
        postLikes.deleteById(id);
        return "The post with the id -"+id+"is deleted ";



    }
    @Override
    @Transactional
    public String addLike(Long postId, String userKey) {

        if (userKey == null || userKey.isBlank()) return "userKey is required";

        if (existsByPostIdAndUserKey(postId, userKey)) {
            return "Already liked";
        }

        PostLikes like = new PostLikes();
        like.setPostId(postId);
        like.setUserKey(userKey);

        entity.persist(like);

        return "Liked";
    }
}
