package com.jomap.backend.Services.Community.Posts;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Community.Posts.Comments.PostCommentService;
import com.jomap.backend.Services.Community.Posts.Likes.PostLikeService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostServiceImpl implements PostsServices {

    private final PostRepository     postRepository;
    private final UserRepository     userRepository;
    private final PostLikeService    likesService;
    private final PostCommentService commentsService;

    // ─────────────────────────────────────────────────────────────────────────
    // ALGORITHM WEIGHTS  (each mode must sum to 1.0)
    // ─────────────────────────────────────────────────────────────────────────

    // Warm-start: user has interaction history → personal interests lead
    private static final double W_INTEREST_WARM   = 0.45;
    private static final double W_LOCATION_WARM   = 0.30;
    private static final double W_ENGAGEMENT_WARM = 0.15;
    private static final double W_RECENCY_WARM    = 0.10;

    // Cold-start: brand-new user with no history → nearby + popular
    private static final double W_LOCATION_COLD   = 0.50;
    private static final double W_ENGAGEMENT_COLD = 0.30;
    private static final double W_RECENCY_COLD    = 0.20;

    // Location: characteristic radius for exponential decay (km)
    private static final double NEAR_RADIUS_KM         = 50.0;

    // Engagement: what we consider "fully viral" for log-normalization
    private static final double ENGAGEMENT_CAP          = 500.0;

    // Recency: half-life in hours (score halves every N hours)
    private static final double RECENCY_HALF_LIFE_HOURS = 24.0;

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getAllPosts() {
        List<PostResponse> responses = postRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .map(p -> toResponse(p, null, null))
                .toList();

        return ApiResponse.success("Posts fetched successfully", responses);
    }

    @Override
    @Transactional
    public ApiResponse<PostResponse> getPostById(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        return ApiResponse.success("Post fetched successfully", toResponse(post, null, null));
    }

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getFeedSummary(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<PostResponse> responses = postRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .map(p -> toResponse(p, null, null))
                .toList();

        return ApiResponse.success("Feed fetched successfully", responses);
    }

    @Override
    @Transactional
    public ApiResponse<PostResponse> createPost(String emailFromToken, CreatePostRequest request) {
        User author = userRepository.findByEmail(emailFromToken).orElse(null);
        if (author == null) {
            return ApiResponse.error("User not found");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.error("Content is required");
        }

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.getContent().trim());
        post.setMediaUrl(request.getMediaUrl());

        // Type
        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                post.setType(Post.PostType.valueOf(request.getType().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("Invalid post type: must be COMMUNITY, EVENT, or OFFER");
            }
        } else {
            post.setType(Post.PostType.COMMUNITY);
        }

        // Category & coordinates — now properly wired from the DTO
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            post.setCategory(request.getCategory().trim().toUpperCase());
        }
        post.setLatitude(request.getLatitude());
        post.setLongitude(request.getLongitude());

        Post saved = postRepository.save(post);
        return ApiResponse.success("Post created successfully", toResponse(saved, null, null));
    }

    @Override
    @Transactional
    public ApiResponse<PostResponse> updatePost(String emailFromToken,
                                                Long postId,
                                                UpdatePostRequest request) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only update your own post");
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return ApiResponse.error("Content is required");
        }

        post.setContent(request.getContent().trim());
        post.setMediaUrl(request.getMediaUrl());

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                post.setType(Post.PostType.valueOf(request.getType().trim().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("Invalid post type: must be COMMUNITY, EVENT, or OFFER");
            }
        }

        // Category & coordinates — now properly wired from the DTO
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            post.setCategory(request.getCategory().trim().toUpperCase());
        }
        if (request.getLatitude() != null)  post.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) post.setLongitude(request.getLongitude());

        Post saved = postRepository.save(post);
        return ApiResponse.success("Post updated successfully", toResponse(saved, null, null));
    }

    @Override
    @Transactional
    public ApiResponse<String> deletePost(String emailFromToken, Long postId) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        Post post = postRepository.findById(postId).orElse(null);
        if (post == null || Boolean.TRUE.equals(post.getIsDeleted())) {
            return ApiResponse.error("Post not found");
        }

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            return ApiResponse.error("You can only delete your own post");
        }

        post.setIsDeleted(true);
        postRepository.save(post);

        return ApiResponse.success("Post deleted successfully", "Post deleted successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PERSONALIZED FEED  — main algorithm
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getPersonalizedFeed(
            String emailFromToken,
            double userLat,
            double userLng,
            int page,
            int size
    ) {
        // 1. Resolve current user
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        // 2. Load all active posts — no date window
        //    (recency decay handles age; a hard cutoff would hide relevant old posts)
        List<Post> candidates = postRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .toList();

        if (candidates.isEmpty()) {
            return ApiResponse.success("Personalized feed fetched successfully", List.of());
        }

        // 3. Build normalized interest profile from THIS user's own interactions
        //    Map: category → weight in [0.0, 1.0]
        Map<String, Double> interestProfile = buildUserInterestProfile(currentUser.getId());
        boolean isColdStart = interestProfile.isEmpty();

        // 4. Pre-fetch engagement counts in bulk — avoids N+1 queries
        List<Long> postIds = candidates.stream().map(Post::getId).toList();
        Map<Long, Long> likeCounts    = likesService.countByPostIds(postIds);
        Map<Long, Long> commentCounts = commentsService.countByPostIds(postIds);

        // 5. Score and sort
        List<ScoredPost> scoredPosts = candidates.stream()
                .map(post -> scorePost(post, interestProfile, isColdStart,
                        userLat, userLng, likeCounts, commentCounts))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .toList();

        // 6. Paginate on the sorted result
        int fromIndex = page * size;
        if (fromIndex >= scoredPosts.size()) {
            return ApiResponse.success("Personalized feed fetched successfully", List.of());
        }
        int toIndex = Math.min(fromIndex + size, scoredPosts.size());

        List<PostResponse> responses = scoredPosts.subList(fromIndex, toIndex)
                .stream()
                .map(sp -> toResponse(sp.post(), sp.distanceKm(), sp.scoreReason()))
                .toList();

        return ApiResponse.success("Personalized feed fetched successfully", responses);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTEREST PROFILE BUILDER
    //
    // Reads only this user's liked and commented posts.
    // Signals:  like = 1 pt  |  comment = 1.5 pt (commenting shows stronger intent)
    // Result is normalized to [0, 1] so it competes fairly with the other
    // sub-scores in the final weighted formula.
    // ─────────────────────────────────────────────────────────────────────────

    private Map<String, Double> buildUserInterestProfile(Long userId) {
        Map<String, Double> raw = new HashMap<>();

        // Posts the user has LIKED
        for (Long postId : likesService.getPostIdsLikedByUser(userId)) {
            postRepository.findById(postId).ifPresent(post -> {
                if (!Boolean.TRUE.equals(post.getIsDeleted()) && post.getCategory() != null) {
                    raw.merge(post.getCategory().toUpperCase(), 1.0, Double::sum);
                }
            });
        }

        // Posts the user has COMMENTED ON
        for (Long postId : commentsService.getPostIdsCommentedByUser(userId)) {
            postRepository.findById(postId).ifPresent(post -> {
                if (!Boolean.TRUE.equals(post.getIsDeleted()) && post.getCategory() != null) {
                    raw.merge(post.getCategory().toUpperCase(), 1.5, Double::sum);
                }
            });
        }

        if (raw.isEmpty()) return Collections.emptyMap();

        // Normalize: divide by max so every weight lands in [0, 1]
        double maxWeight = Collections.max(raw.values());
        if (maxWeight == 0) return Collections.emptyMap();

        raw.replaceAll((cat, w) -> w / maxWeight);
        return raw;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PER-POST SCORING
    // ─────────────────────────────────────────────────────────────────────────

    private ScoredPost scorePost(
            Post post,
            Map<String, Double> interestProfile,
            boolean isColdStart,
            double userLat, double userLng,
            Map<Long, Long> likeCounts,
            Map<Long, Long> commentCounts
    ) {
        double interestScore   = computeInterestScore(post, interestProfile);
        double locationScore   = computeLocationScore(post, userLat, userLng);
        double engagementScore = computeEngagementScore(post, likeCounts, commentCounts);
        double recencyScore    = computeRecencyScore(post);

        double finalScore = isColdStart
                ? (locationScore   * W_LOCATION_COLD)
                + (engagementScore * W_ENGAGEMENT_COLD)
                + (recencyScore    * W_RECENCY_COLD)

                : (interestScore   * W_INTEREST_WARM)
                + (locationScore   * W_LOCATION_WARM)
                + (engagementScore * W_ENGAGEMENT_WARM)
                + (recencyScore    * W_RECENCY_WARM);

        Double distanceKm  = computeDistanceKm(post, userLat, userLng);
        String scoreReason = buildScoreReason(interestScore, locationScore, distanceKm, isColdStart);

        return new ScoredPost(post, finalScore, distanceKm, scoreReason);
    }

    // ── Interest ──────────────────────────────────────────────────────────────
    // Returns the normalized category weight [0, 1] — 0 if no category / no data

    private double computeInterestScore(Post post, Map<String, Double> profile) {
        if (post.getCategory() == null || profile.isEmpty()) return 0.0;
        return profile.getOrDefault(post.getCategory().toUpperCase(), 0.0);
    }

    // ── Location ──────────────────────────────────────────────────────────────
    // Exponential decay:  score = e^(-distance / NEAR_RADIUS_KM)
    //   0 km  → 1.00  |  50 km → 0.37  |  100 km → 0.14  |  200 km → 0.02
    // Posts with no coordinates score 0 but can still surface via interest/engagement.

    private double computeLocationScore(Post post, double userLat, double userLng) {
        if (post.getLatitude() == null || post.getLongitude() == null) return 0.0;
        double distKm = haversineKm(userLat, userLng, post.getLatitude(), post.getLongitude());
        return Math.exp(-distKm / NEAR_RADIUS_KM);
    }

    private Double computeDistanceKm(Post post, double userLat, double userLng) {
        if (post.getLatitude() == null || post.getLongitude() == null) return null;
        return haversineKm(userLat, userLng, post.getLatitude(), post.getLongitude());
    }

    // ── Engagement ────────────────────────────────────────────────────────────
    // Log-normalized so viral posts don't infinitely dominate moderate ones.
    // raw = likes*0.4 + comments*0.6
    // score = log(1 + raw) / log(1 + ENGAGEMENT_CAP)  →  [0, 1]

    private double computeEngagementScore(Post post,
                                          Map<Long, Long> likeCounts,
                                          Map<Long, Long> commentCounts) {
        double likes    = likeCounts.getOrDefault(post.getId(), 0L);
        double comments = commentCounts.getOrDefault(post.getId(), 0L);
        double raw      = (likes * 0.4) + (comments * 0.6);
        return Math.min(Math.log1p(raw) / Math.log1p(ENGAGEMENT_CAP), 1.0);
    }

    // ── Recency ───────────────────────────────────────────────────────────────
    // Exponential decay with 24-hour half-life.
    // score = 0.5^(hoursOld / RECENCY_HALF_LIFE_HOURS)
    //   0 h → 1.00  |  24 h → 0.50  |  72 h → 0.125  |  7 days → ~0.02

    private double computeRecencyScore(Post post) {
        if (post.getCreatedAt() == null) return 0.0;
        long hoursOld = Math.max(Duration.between(post.getCreatedAt(), Instant.now()).toHours(), 0);
        return Math.pow(0.5, (double) hoursOld / RECENCY_HALF_LIFE_HOURS);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HAVERSINE DISTANCE
    // ─────────────────────────────────────────────────────────────────────────

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCORE REASON  (human-readable label returned to the frontend)
    // ─────────────────────────────────────────────────────────────────────────

    private String buildScoreReason(double interestScore,
                                    double locationScore,
                                    Double distanceKm,
                                    boolean isColdStart) {
        boolean hasInterest = interestScore > 0.05;   // >5 % match threshold
        boolean isNear      = locationScore  > 0.30;  // within ~60 km

        String distLabel = distanceKm != null
                ? String.format("%.1f km away", distanceKm)
                : "near you";

        if (isColdStart && isNear)  return "Popular near you · " + distLabel;
        if (hasInterest && isNear)  return "Matches your interests · " + distLabel;
        if (hasInterest)            return "Matches your interests";
        if (isNear)                 return "Near you · " + distLabel;
        return "Popular post";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESPONSE MAPPER
    // ─────────────────────────────────────────────────────────────────────────

    private PostResponse toResponse(Post post, Double distanceKm, String scoreReason) {
        PostResponse r = new PostResponse();
        r.setId(post.getId());
        r.setContent(post.getContent());
        r.setMediaUrl(post.getMediaUrl());
        r.setType(post.getType() != null ? post.getType().name() : null);
        r.setCreatedAt(post.getCreatedAt());
        r.setUpdatedAt(post.getUpdatedAt());
        r.setCategory(post.getCategory());
        r.setLatitude(post.getLatitude());
        r.setLongitude(post.getLongitude());
        r.setDistanceKm(distanceKm);
        r.setScoreReason(scoreReason);

        if (post.getAuthor() != null) {
            r.setAuthorId(post.getAuthor().getId());
            r.setAuthorEmail(post.getAuthor().getEmail());
            r.setAuthorUsername(post.getAuthor().getUsername());
            r.setAuthorProfileImageUrl(post.getAuthor().getProfileImageUrl());
        }

        r.setLikeCount(likesService.countByPostId(post.getId()).getData());
        r.setCommentCount(commentsService.countByPostId(post.getId()).getData());
        return r;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL RECORD
    // ─────────────────────────────────────────────────────────────────────────

    private record ScoredPost(
            Post post,
            double score,
            Double distanceKm,
            String scoreReason
    ) {}
}