package com.start.demo.Services.Community.Stories.Interaction;

import com.start.demo.Entities.Stories.Story;
import com.start.demo.Entities.Stories.StoryReply;
import com.start.demo.Entities.Users.User;
import com.start.demo.Exciptions.BadRequestException;
import com.start.demo.Exciptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoryRepliesServiceImpl implements StoryRepliesService {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public Long countByStoryId(Long storyId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(r) FROM StoryReply r WHERE r.story.id = :storyId AND r.isDeleted = false",
                Long.class
        );
        q.setParameter("storyId", storyId);
        return q.getSingleResult();
    }

    @Override
    public List<StoryReply> findByStoryId(Long storyId) {
        TypedQuery<StoryReply> q = entity.createQuery(
                "FROM StoryReply r WHERE r.story.id = :storyId AND r.isDeleted = false ORDER BY r.createdAt DESC",
                StoryReply.class
        );
        q.setParameter("storyId", storyId);
        return q.getResultList();
    }

    @Override
    @Transactional
    public StoryReply addReply(Long storyId, String content) {

        if (content == null || content.isBlank()) {
            throw new BadRequestException("Content is required");
        }

        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            throw new ResourceNotFoundException("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();

        StoryReply reply = new StoryReply();
        reply.setStory(story);
        reply.setUser(currentUser);
        reply.setContent(content);

        entity.persist(reply);
        return reply;
    }

    @Override
    @Transactional
    public StoryReply updateReply(Long replyId, String content) {

        StoryReply existing = entity.find(StoryReply.class, replyId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new ResourceNotFoundException("Reply not found with id: " + replyId);
        }

        if (content == null || content.isBlank()) {
            throw new BadRequestException("Content is required");
        }

        User currentUser = getCurrentUser();

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only edit your own reply");
        }

        existing.setContent(content);
        return existing;
    }

    @Override
    @Transactional
    public String deleteReply(Long replyId) {

        StoryReply existing = entity.find(StoryReply.class, replyId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new ResourceNotFoundException("Reply not found with id: " + replyId);
        }

        User currentUser = getCurrentUser();

        if (!existing.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only delete your own reply");
        }

        existing.setDeleted(true);
        return "Deleted successfully";
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