package com.start.demo.DTOs.Posts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdatePostRequest {

    @NotBlank
    @Size(max = 2000)
    private String content;

    @Size(max = 500)
    private String mediaUrl;

    private String type; // COMMUNITY / EVENT / OFFER

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}