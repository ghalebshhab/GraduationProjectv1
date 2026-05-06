package com.jomap.backend.DTOs.Dashboard;

import lombok.Data;

@Data
public class AdminPostResponse {

    private Long id;
    private String content;
    private String type;
    private Boolean deleted;
    private Long authorId;
    private String authorName;
}