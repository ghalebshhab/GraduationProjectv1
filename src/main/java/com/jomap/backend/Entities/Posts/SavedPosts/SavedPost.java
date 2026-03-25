package com.jomap.backend.Entities.Posts.SavedPosts;

import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "saved_posts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_saved_post_user_post", columnNames = {"user_id", "post_id"})
        }

)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SavedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user who saved the post
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // saved post
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SavedPost(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }






}