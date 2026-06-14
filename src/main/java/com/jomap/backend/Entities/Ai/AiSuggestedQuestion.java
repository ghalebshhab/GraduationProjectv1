package com.jomap.backend.Entities.Ai;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ai_suggested_questions")
@Data
public class AiSuggestedQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false)
    private boolean active = true;
}
