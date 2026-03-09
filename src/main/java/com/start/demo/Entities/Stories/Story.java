package com.start.demo.Entities.Stories;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.start.demo.Controllers.Story.StoryLikesController;
import com.start.demo.Entities.Users.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stories")
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many stories belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // One story can have many views
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<StoryView> views = new ArrayList<>();

    // One story can have many reactions
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonIgnore
    private List<StoryLike> reactions = new ArrayList<>();

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(length = 500)
    private String caption;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusSeconds(24 * 60 * 60);
        }
    }

    public Story() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<StoryView> getViews() {
        return views;
    }

    public void setViews(List<StoryView> views) {
        this.views = views;
    }

    public List<StoryLike> getReactions() {
        return reactions;
    }

    public void setReactions(List<StoryLike> reactions) {
        this.reactions = reactions;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    @Transient
    public Long getAuthorId() {
        return author != null ? author.getId() : null;
    }
}