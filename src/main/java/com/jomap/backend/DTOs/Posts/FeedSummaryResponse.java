package com.jomap.backend.DTOs.Posts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedSummaryResponse {
    private List<PostResponse> USER;
    private List<PostResponse> ACTIVITY;
    private List<PostResponse> OWNER;
    private List<PostResponse> OFFER;
}
