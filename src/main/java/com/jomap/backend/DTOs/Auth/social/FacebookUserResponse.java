package com.jomap.backend.DTOs.Auth.social;

public record FacebookUserResponse(
        String id,
        String name,
        String email,
        FacebookPicture picture
) {
    public record FacebookPicture(
            FacebookPictureData data
    ) {}

    public record FacebookPictureData(
            String url
    ) {}
}