package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.StoryLike;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class StoryLikesServiceImpl implements StoryLikesService {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public ApiResponse<Long> countByStoryId(Long storyId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(l) FROM StoryLike l WHERE l.story.id = :storyId",
                Long.class
        );
        q.setParameter("storyId", storyId);

        return ApiResponse.success("Story likes count fetched successfully", q.getSingleResult());
    }

    @Override
    public ApiResponse<Boolean> existsByStoryId(Long storyId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(l) FROM StoryLike l WHERE l.story.id = :storyId AND l.user.id = :userId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        return ApiResponse.success("Story like existence checked successfully", q.getSingleResult() > 0);
    }

    @Override
    @Transactional
    public ApiResponse<String> addLike(Long storyId) {
        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            return ApiResponse.error("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(l) FROM StoryLike l WHERE l.story.id = :storyId AND l.user.id = :userId",
                Long.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        if (q.getSingleResult() > 0) {
            return ApiResponse.error("User already liked this story");
        }

        StoryLike like = new StoryLike();
        like.setStory(story);
        like.setUser(currentUser);
        entity.persist(like);

        return ApiResponse.success("Story liked successfully", "Story liked successfully");
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteByStoryId(Long storyId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        TypedQuery<StoryLike> q = entity.createQuery(
                "FROM StoryLike l WHERE l.story.id = :storyId AND l.user.id = :userId",
                StoryLike.class
        );
        q.setParameter("storyId", storyId);
        q.setParameter("userId", currentUser.getId());

        List<StoryLike> list = q.getResultList();

        if (list.isEmpty()) {
            return ApiResponse.error("Like not found for this user on story id: " + storyId);
        }

        entity.remove(list.get(0));
        return ApiResponse.success("Story unliked successfully", "Story unliked successfully");
    }

    private User getCurrentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        String email = authentication.getName();

        TypedQuery<User> q = entity.createQuery(
                "SELECT u FROM User u WHERE u.email = :email",
                User.class
        );
        q.setParameter("email", email);

        List<User> users = q.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }
}