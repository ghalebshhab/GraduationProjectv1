package com.start.demo.DTOs.Stories;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateStoryRequest {


    @NotBlank
    @Size(max = 500)
    private String mediaUrl;

    @Size(max = 500)
    private String caption;

    // optional: hours (default 24) - simplest control
    private Integer expiresInHours;



    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public Integer getExpiresInHours() { return expiresInHours; }
    public void setExpiresInHours(Integer expiresInHours) { this.expiresInHours = expiresInHours; }
}