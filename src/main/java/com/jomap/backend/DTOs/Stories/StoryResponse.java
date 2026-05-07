package com.jomap.backend.DTOs.Stories;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryResponse {

    private Long id;
    private Long authorId;
    private String mediaUrl;
    private String caption;
    private Instant createdAt;
    private Instant expiresAt;


}