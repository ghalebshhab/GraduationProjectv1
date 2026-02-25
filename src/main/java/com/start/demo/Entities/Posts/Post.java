package com.start.demo.Entities.Posts;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "posts")
public class Post {

    public enum PostType { COMMUNITY, EVENT, OFFER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // مؤقتًا بدون security: نخزن ايميل/اسم الكاتب كنص
    @Column(nullable = false, length = 120)
    private String authorEmail;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(length = 500)
    private String mediaUrl; // optional

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type = PostType.COMMUNITY;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Post() {}

    public Post(String authorEmail, String content, String mediaUrl, PostType type) {
        this.authorEmail = authorEmail;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}