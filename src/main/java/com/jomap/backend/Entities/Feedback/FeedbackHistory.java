package com.jomap.backend.Entities.Feedback;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feedback_history")
@Getter
@Setter
@NoArgsConstructor
public class FeedbackHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(name = "previous_rating")
    private Integer previousRating;

    @Column(name = "previous_comment", length = 2000)
    private String previousComment;

    @Column(name = "previous_owner_reply", length = 2000)
    private String previousOwnerReply;

    @Column(name = "edited_at", nullable = false)
    private LocalDateTime editedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "edit_type", nullable = false)
    private EditType editType;

    @PrePersist
    public void beforeCreate() {
        editedAt = LocalDateTime.now();
    }
}
