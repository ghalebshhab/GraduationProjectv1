package com.jomap.backend.Services.Community.Posts.Share;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Share.PostShareLinkResponse;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostShareService {

    private final PostRepository postRepository;


    public ApiResponse<PostShareLinkResponse> generateShareLink(Long postId) {

        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty()) {
            return ApiResponse.error("Post not found");
        }

        Post post = optionalPost.get();

        String baseUrl = "https://jomap.app";

        String shareUrl = baseUrl + "/posts/" + post.getId();

        String shareText = "Check this post on JoMap: " + shareUrl;

        PostShareLinkResponse response = new PostShareLinkResponse(
                post.getId(),
                shareUrl,
                shareText
        );

        return ApiResponse.success("Share link generated successfully", response);
    }
}