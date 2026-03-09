package com.start.demo.Entities.Stories;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.start.demo.Entities.Users.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "story_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"story_id", "user_id"})
)
public class StoryLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many story likes belong to one story
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    @JsonIgnore
    private Story story;

    // Many story likes belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public StoryLike() {}

    public Long getId() {
        return id;
    }

    public Story getStory() {
        return story;
    }

    public void setStory(Story story) {
        this.story = story;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Transient
    public Long getStoryId() {
        return story != null ? story.getId() : null;
    }

    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}