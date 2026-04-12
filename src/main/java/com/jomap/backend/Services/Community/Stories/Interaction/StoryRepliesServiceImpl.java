package com.jomap.backend.Services.Community.Stories.Interaction;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Stories.Replies.StoryReplyResponse;
import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.StoryReply;
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
public class StoryRepliesServiceImpl implements StoryRepliesService {

    @PersistenceContext
    private EntityManager entity;

    @Override
    public ApiResponse<Long> countByStoryId(Long storyId) {
        TypedQuery<Long> q = entity.createQuery(
                "SELECT COUNT(r) FROM StoryReply r WHERE r.story.id = :storyId AND r.isDeleted = false",
                Long.class
        );
        q.setParameter("storyId", storyId);

        return ApiResponse.success("Story replies count fetched successfully", q.getSingleResult());
    }

    @Override
    public ApiResponse<List<StoryReplyResponse>> findByStoryId(Long storyId) {
        TypedQuery<StoryReply> q = entity.createQuery(
                "FROM StoryReply r WHERE r.story.id = :storyId AND r.isDeleted = false ORDER BY r.createdAt DESC",
                StoryReply.class
        );
        q.setParameter("storyId", storyId);

        List<StoryReplyResponse> responses = q.getResultList()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("Story replies fetched successfully", responses);
    }

    @Override
    @Transactional
    public ApiResponse<StoryReplyResponse> addReply(Long storyId, String content) {
        if (content == null || content.isBlank()) {
            return ApiResponse.error("Content is required");
        }

        Story story = entity.find(Story.class, storyId);
        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            return ApiResponse.error("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        StoryReply reply = new StoryReply();
        reply.setStory(story);
        reply.setUser(currentUser);
        reply.setContent(content.trim());

        entity.persist(reply);

        return ApiResponse.success("Story reply added successfully", mapToResponse(reply));
    }

    @Override
    @Transactional
    public ApiResponse<StoryReplyResponse> updateReply(Long replyId, String content) {
        StoryReply existing = entity.find(StoryReply.class, replyId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            return ApiResponse.error("Reply not found with id: " + replyId);
        }

        if (content == null || content.isBlank()) {
            return ApiResponse.error("Content is required");
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        if (existing.getUser() == null || !existing.getUser().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only edit your own reply");
        }

        existing.setContent(content.trim());

        return ApiResponse.success("Story reply updated successfully", mapToResponse(existing));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteReply(Long replyId) {
        StoryReply existing = entity.find(StoryReply.class, replyId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            return ApiResponse.error("Reply not found with id: " + replyId);
        }

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Authenticated user not found");
        }

        if (existing.getUser() == null || !existing.getUser().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only delete your own reply");
        }

        existing.setDeleted(true);

        return ApiResponse.success("Story reply deleted successfully", "Story reply deleted successfully");
    }

    private StoryReplyResponse mapToResponse(StoryReply reply) {
        return new StoryReplyResponse(
                reply.getId(),
                reply.getStory() != null ? reply.getStory().getId() : null,
                reply.getUser() != null ? reply.getUser().getId() : null,
                reply.getUser() != null ? reply.getUser().getUsername() : null,
                reply.getContent(),
                reply.getDeleted(),
                reply.getCreatedAt()
        );
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