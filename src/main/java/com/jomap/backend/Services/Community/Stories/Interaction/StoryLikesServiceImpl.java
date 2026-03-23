package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.StoryLike;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Exciptions.DuplicateResourceException;
import com.jomap.backend.Exciptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoryLikesServiceImpl implements StoryLikesService {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public Long countByStoryId(Long storyId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(l) FROM StoryLike l WHERE l.story.id = :storyId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        return q.getSingleResult();
    }

    @Override
    public Boolean existsByStoryId(Long storyId) {
        User currentUser = getCurrentUser();

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(l) FROM StoryLike l WHERE l.story.id = :storyId AND l.user.id = :userId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        return q.getSingleResult() > 0;
    }

    @Override
    @Transactional
    public StoryLike addLike(Long storyId) {

        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            throw new ResourceNotFoundException("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();

        if (existsByStoryId(storyId)) {
            throw new DuplicateResourceException("User already liked this story");
        }

        StoryLike like = new StoryLike();
        like.setStory(story);
        like.setUser(currentUser);

        entity.persist(like);

        return like;
    }

    @Override
    @Transactional
    public String deleteByStoryId(Long storyId) {

        User currentUser = getCurrentUser();

        TypedQuery<StoryLike> q = entity.createQuery(
                "FROM StoryLike l WHERE l.story.id = :storyId AND l.user.id = :userId",
                StoryLike.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        List<StoryLike> list = q.getResultList();

        if (list.isEmpty()) {
            throw new ResourceNotFoundException("Like not found for this user on story id: " + storyId);
        }

        entity.remove(list.get(0));
        return "Unliked successfully";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        String email = authentication.getName();

        TypedQuery<User> q = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        q.setParameter("email", email);

        List<User> users = q.getResultList();

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        return users.get(0);
    }
}