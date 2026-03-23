package com.jomap.backend.Services.Community.Stories;

import com.jomap.backend.DTOs.Stories.CreateStoryRequest;
import com.jomap.backend.DTOs.Stories.StoryResponse;
import com.jomap.backend.Entities.Stories.Story;
import com.jomap.backend.Entities.Stories.Storyrepo;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Exciptions.BadRequestException;
import com.jomap.backend.Exciptions.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class StoriesServiceImpl implements StoryService {

    private final Storyrepo storyRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entity;



    @Override
    @Transactional
    public StoryResponse create(CreateStoryRequest request) {

        User author = getCurrentUser();

        if (request.getMediaUrl() == null || request.getMediaUrl().isBlank()) {
            throw new BadRequestException("Media URL is required");
        }

        Story story = new Story();
        story.setAuthor(author);
        story.setMediaUrl(request.getMediaUrl());
        story.setCaption(request.getCaption());

        Integer h = request.getExpiresInHours();
        if (h != null) {
            if (h <= 0) {
                throw new BadRequestException("expiresInHours must be greater than 0");
            }
            story.setExpiresAt(Instant.now().plusSeconds((long) h * 3600));
        }

        Story saved = storyRepository.save(story);
        return toResponse(saved);
    }

    @Override
    public List<StoryResponse> getActiveStories(int page, int size) {

        Instant now = Instant.now();

        TypedQuery<Story> q = entity.createQuery(
                "FROM Story s " +
                        "WHERE s.isDeleted = false AND s.expiresAt > :now " +
                        "ORDER BY s.createdAt DESC",
                Story.class
        );
        q.setParameter("now", now);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<StoryResponse> res = new ArrayList<>();
        for (Story s : q.getResultList()) {
            res.add(toResponse(s));
        }
        return res;
    }

    @Override
    public List<StoryResponse> getActiveStoriesByUser(Long userId, int page, int size) {

        Instant now = Instant.now();

        TypedQuery<Story> q = entity.createQuery(
                "FROM Story s " +
                        "WHERE s.isDeleted = false AND s.expiresAt > :now AND s.author.id = :userId " +
                        "ORDER BY s.createdAt DESC",
                Story.class
        );
        q.setParameter("now", now);
        q.setParameter("userId", userId);
        q.setFirstResult(page * size);
        q.setMaxResults(size);

        List<StoryResponse> res = new ArrayList<>();
        for (Story s : q.getResultList()) {
            res.add(toResponse(s));
        }
        return res;
    }

    @Override
    @Transactional
    public String deleteStory(Long storyId) {
        Story story = entity.find(Story.class, storyId);

        if (story == null || Boolean.TRUE.equals(story.getDeleted())) {
            throw new ResourceNotFoundException("Story not found with id: " + storyId);
        }

        User currentUser = getCurrentUser();

        if (!story.getAuthor().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only delete your own story");
        }

        story.setDeleted(true);
        return "Story deleted successfully";
    }

    private StoryResponse toResponse(Story s) {
        StoryResponse r = new StoryResponse();
        r.setId(s.getId());
        r.setMediaUrl(s.getMediaUrl());
        r.setCaption(s.getCaption());
        r.setCreatedAt(s.getCreatedAt());
        r.setExpiresAt(s.getExpiresAt());

        if (s.getAuthor() != null) {
            r.setAuthorId(s.getAuthor().getId());
            r.setAuthorEmail(s.getAuthor().getEmail());
        }

        return r;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new BadRequestException("Authenticated user not found");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}