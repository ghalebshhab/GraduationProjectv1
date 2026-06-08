package com.jomap.backend.Controllers.Post;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.Share.PostShareLinkResponse;
import com.jomap.backend.Services.Community.Posts.Share.PostShareService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostShareController {

    private final PostShareService postShareService;

    public PostShareController(PostShareService postShareService) {
        this.postShareService = postShareService;
    }

    @GetMapping("/{postId}/share-link")
    public ResponseEntity<ApiResponse<PostShareLinkResponse>> getShareLink(
            @PathVariable Long postId
    ) {
        ApiResponse<PostShareLinkResponse> response =
                postShareService.generateShareLink(postId);

        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(response);
    }
}