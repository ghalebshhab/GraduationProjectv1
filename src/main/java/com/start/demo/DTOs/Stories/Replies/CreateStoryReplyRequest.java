package com.start.demo.DTOs.Stories.Replies;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateStoryReplyRequest {


    @NotBlank
    @Size(max = 1000)
    private String content;



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}