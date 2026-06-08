package com.jomap.backend.DTOs.Posts.Share;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostShareLinkResponse {

    private Long postId;
    private String shareUrl;
    private String shareText;



}