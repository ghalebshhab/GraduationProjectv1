package com.jomap.backend.DTOs.Fav;

import lombok.Data;

@Data
public class FavoritePostDto {
    private Long id;
    private Long authorId; // تم التعديل إلى Long ليطابق الداتابيس
    private String authorName;
    private String authorAvatarUrl;
    private String content;
    private String imageUrl;
    private String createdAt;
    private String category;
    private int likeCount;
    private int commentCount;
}
