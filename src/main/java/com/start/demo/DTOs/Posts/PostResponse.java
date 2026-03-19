package com.start.demo.DTOs.Posts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {

    private Long id;

    private Long authorId;       // ✅ الجديد
    private String authorEmail;  // optional لو بدك تعرضه

    private String content;
    private String mediaUrl;
    private String type;

    private Instant createdAt;

    private Long likeCount;      // ✅ مهم للفيد
    private Long commentCount;   // ✅ مهم للفيد


}