package com.start.demo.Entities.Posts.postLikes;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_key"})
)
public class PostLikes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // نخزنها كـ id مباشرة (بدون علاقة ManyToOne هسا) عشان يكون بسيط
    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_key", nullable = false, length = 80)
    private String userKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }

    public PostLikes() {}

    public PostLikes(Long postId, String userKey) {
        this.postId = postId;
        this.userKey = userKey;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getUserKey() { return userKey; }
    public void setUserKey(String userKey) { this.userKey = userKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}