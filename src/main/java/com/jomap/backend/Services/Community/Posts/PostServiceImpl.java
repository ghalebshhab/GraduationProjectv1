package com.jomap.backend.Services.Community.Posts;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Posts.CreatePostRequest;
import com.jomap.backend.DTOs.Posts.PostResponse;
import com.jomap.backend.DTOs.Posts.UpdatePostRequest;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Community.Posts.Comments.PostCommentService;
import com.jomap.backend.Services.Community.Posts.Likes.PostLikeService;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPostsRepository;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Offers.OfferRepo;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.Activity;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PostServiceImpl implements PostsServices {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeService likesService;
    private final PostCommentService commentsService;
    private final SavedPostsRepository savedPostsRepository;
    private final com.jomap.backend.Entities.Locations.LocationRepo locationRepo;
    private final ActivityRepository activityRepository;
    private final OfferRepo offerRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // ALGORITHM WEIGHTS (each mode must sum to 1.0)
    // ─────────────────────────────────────────────────────────────────────────

    // Warm-start: user has interaction history → personal interests lead
    private static final double W_INTEREST_WARM = 0.45;
    private static final double W_LOCATION_WARM = 0.30;
    private static final double W_ENGAGEMENT_WARM = 0.15;
    private static final double W_RECENCY_WARM = 0.10;

    // Cold-start: brand-new user with no history → nearby + popular
    private static final double W_LOCATION_COLD = 0.50;
    private static final double W_ENGAGEMENT_COLD = 0.30;
    private static final double W_RECENCY_COLD = 0.20;

    // Location: characteristic radius for exponential decay (km)
    private static final double NEAR_RADIUS_KM = 50.0;

    // Engagement: what we consider "fully viral" for log-normalization
    private static final double ENGAGEMENT_CAP = 500.0;

    // Recency: half-life in hours (score halves every N hours)
    private static final double RECENCY_HALF_LIFE_HOURS = 24.0;

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getAllPosts(int userId, String Category) {
       List<PostResponse> responses = postRepository
            .findActivePostsByUserId(userId, Category)
            .stream()
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
    public ApiResponse<com.jomap.backend.DTOs.Posts.FeedSummaryResponse> getFeedSummary(String emailFromToken, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        User user = null;
        if (emailFromToken != null && !emailFromToken.isBlank()) {
            user = userRepository.findByEmail(emailFromToken).orElse(null);
        }
        final User currentUser = user;

        List<PostResponse> responses = postRepository.findAll(pageable)
                .getContent()
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .map(p -> toResponse(p, null, null))
                .toList();

        if (currentUser != null) {
            List<Long> likedPostIds = likesService.getPostIdsLikedByUser(currentUser.getId());
            List<Long> savedPostIds = savedPostsRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                    .stream().map(sp -> sp.getPost().getId()).toList();

            responses.forEach(r -> {
                r.setLikedByCurrentUser(likedPostIds.contains(r.getId()));
                r.setSavedByCurrentUser(savedPostIds.contains(r.getId()));
            });
        }

        com.jomap.backend.DTOs.Posts.FeedSummaryResponse summary = new com.jomap.backend.DTOs.Posts.FeedSummaryResponse();
        summary.setUSER(responses.stream().filter(p -> "USER".equalsIgnoreCase(p.getCategory())).toList());
        summary.setACTIVITY(responses.stream().filter(p -> "ACTIVITY".equalsIgnoreCase(p.getCategory())).toList());
        summary.setOWNER(responses.stream().filter(p -> "OWNER".equalsIgnoreCase(p.getCategory())).toList());
        summary.setOFFER(responses.stream().filter(p -> "OFFER".equalsIgnoreCase(p.getCategory())).toList());

        return ApiResponse.success("Feed fetched successfully", summary);
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
                return ApiResponse.error("Invalid post type: must be COMMUNITY, Activity, or OFFER");
            }
        } else {
            post.setType(Post.PostType.COMMUNITY);
        }

        // Category & coordinates — now properly wired from the DTO
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            String category = request.getCategory().trim().toUpperCase();
            if ("OWNER".equals(category)) {
                com.jomap.backend.Entities.Locations.LocationList location = locationRepo.findByOwnerId(author.getId()).orElse(null);
                if (location == null) {
                    return ApiResponse.error("عذراً، يجب أن تمتلك منشأة لتتمكن من النشر كمالك");
                }
                if (location.getStatus() != com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED) {
                    return ApiResponse.error("عذراً، يجب أن تكون حالة المنشأة منشورة (PUBLISHED) لتتمكن من نشر مشاركة");
                }
            }
            post.setCategory(category);
        }
        post.setLatitude(request.getLatitude());
        post.setLongitude(request.getLongitude());

        Post saved = postRepository.save(post);
        return ApiResponse.success("Post created successfully", toResponse(saved, null, null));
    }

    @Override
    @Transactional
    public ApiResponse<PostResponse> createActivityPost(String emailFromToken, com.jomap.backend.DTOs.Posts.CreateActivityPostRequest request) {
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

        post.setCategory("ACTIVITY"); // Set category as ACTIVITY based on user request

        if (request.getType() != null && !request.getType().isBlank()) {
            try {
                String typeStr = request.getType().trim().toUpperCase().replace(" ", "_");
                post.setType(Post.PostType.valueOf(typeStr));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("Invalid type for activity post: must be OWNER, LIVE_COVERAGE, ADS_ACTIVITY, or POST");
            }
        } else {
            post.setType(Post.PostType.OWNER); // fallback
        }
        
        post.setLatitude(request.getLatitude());
        post.setLongitude(request.getLongitude());
        post.setActivityId(request.getActivityId());

        Post saved = postRepository.save(post);
        return ApiResponse.success("Activity Post created successfully", toResponse(saved, null, null));
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
                String typeStr = request.getType().trim().toUpperCase().replace(" ", "_");
                post.setType(Post.PostType.valueOf(typeStr));
            } catch (IllegalArgumentException ex) {
                return ApiResponse.error("Invalid post type: must be COMMUNITY, ACTIVITY, OFFER, USER, OWNER, LIVE_COVERAGE, ADS_ACTIVITY, or POST");
            }
        }

        // Category & coordinates — now properly wired from the DTO
        // Note: category should never be changed after create
        if (request.getLatitude() != null)
            post.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            post.setLongitude(request.getLongitude());

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
    // PERSONALIZED FEED — main algorithm
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getPersonalizedFeed(
            String emailFromToken,
            double userLat,
            double userLng,
            int page,
            int size) {
        // 1. Resolve current user
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        // 2. Load all active posts — no date window
        // (recency decay handles age; a hard cutoff would hide relevant old posts)
        List<Post> candidates = postRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .toList();

        if (candidates.isEmpty()) {
            return ApiResponse.success("Personalized feed fetched successfully", List.of());
        }

        // 3. Build normalized interest profile from THIS user's own interactions
        // Map: category → weight in [0.0, 1.0]
        Map<String, Double> interestProfile = buildUserInterestProfile(currentUser.getId());
        boolean isColdStart = interestProfile.isEmpty();

        // 4. Pre-fetch engagement counts in bulk — avoids N+1 queries
        List<Long> postIds = candidates.stream().map(Post::getId).toList();
        Map<Long, Long> likeCounts = likesService.countByPostIds(postIds);
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

    @Override
    @Transactional
    public ApiResponse<com.jomap.backend.DTOs.PaginatedResponse<PostResponse>> getUnifiedFeed(
            String emailFromToken,
            Double userLat,
            Double userLng,
            int page,
            int size) {
        
        User currentUser = null;
        if (emailFromToken != null && !emailFromToken.isBlank()) {
            currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        }

        List<Post> candidates = postRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsDeleted()))
                .toList();

        if (candidates.isEmpty()) {
            return ApiResponse.success("Feed fetched successfully", 
                new com.jomap.backend.DTOs.PaginatedResponse<>(List.of(), page, size, 0, 0, true));
        }

        Map<String, Double> interestProfile = currentUser != null ? buildUserInterestProfile(currentUser.getId()) : Collections.emptyMap();
        boolean isColdStart = interestProfile.isEmpty();

        List<Long> postIds = candidates.stream().map(Post::getId).toList();
        Map<Long, Long> likeCounts = likesService.countByPostIds(postIds);
        Map<Long, Long> commentCounts = commentsService.countByPostIds(postIds);

        List<ScoredPost> scoredPosts = candidates.stream()
                .map(post -> scorePost(post, interestProfile, isColdStart,
                        userLat, userLng, likeCounts, commentCounts))
                .sorted(Comparator.comparingDouble(ScoredPost::score).reversed())
                .toList();

        int totalElements = scoredPosts.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        
        List<PostResponse> responses;
        if (fromIndex >= totalElements) {
            responses = List.of();
        } else {
            int toIndex = Math.min(fromIndex + size, totalElements);
            responses = scoredPosts.subList(fromIndex, toIndex)
                    .stream()
                    .map(sp -> toResponse(sp.post(), sp.distanceKm(), sp.scoreReason()))
                    .toList();
        }

        if (currentUser != null && !responses.isEmpty()) {
            List<Long> likedPostIds = likesService.getPostIdsLikedByUser(currentUser.getId());
            List<Long> savedPostIds = savedPostsRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                    .stream().map(sp -> sp.getPost().getId()).toList();

            responses.forEach(r -> {
                r.setLikedByCurrentUser(likedPostIds.contains(r.getId()));
                r.setSavedByCurrentUser(savedPostIds.contains(r.getId()));
            });
        }

        boolean last = (page + 1) >= totalPages;
        
        com.jomap.backend.DTOs.PaginatedResponse<PostResponse> paginated = 
            new com.jomap.backend.DTOs.PaginatedResponse<>(responses, page, size, totalElements, totalPages, last);

        return ApiResponse.success("Feed fetched successfully", paginated);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTEREST PROFILE BUILDER
    //
    // Reads only this user's liked and commented posts.
    // Signals: like = 1 pt | comment = 1.5 pt (commenting shows stronger intent)
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

        if (raw.isEmpty())
            return Collections.emptyMap();

        // Normalize: divide by max so every weight lands in [0, 1]
        double maxWeight = Collections.max(raw.values());
        if (maxWeight == 0)
            return Collections.emptyMap();

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
            Double userLat, Double userLng,
            Map<Long, Long> likeCounts,
            Map<Long, Long> commentCounts) {
        double interestScore = computeInterestScore(post, interestProfile);
        double locationScore = computeLocationScore(post, userLat, userLng);
        double engagementScore = computeEngagementScore(post, likeCounts, commentCounts);
        double recencyScore = computeRecencyScore(post);

        double finalScore = isColdStart
                ? (locationScore * W_LOCATION_COLD)
                        + (engagementScore * W_ENGAGEMENT_COLD)
                        + (recencyScore * W_RECENCY_COLD)

                : (interestScore * W_INTEREST_WARM)
                        + (locationScore * W_LOCATION_WARM)
                        + (engagementScore * W_ENGAGEMENT_WARM)
                        + (recencyScore * W_RECENCY_WARM);

        Double distanceKm = computeDistanceKm(post, userLat, userLng);
        String scoreReason = buildScoreReason(interestScore, locationScore, distanceKm, isColdStart);

        return new ScoredPost(post, finalScore, distanceKm, scoreReason);
    }

    // ── Interest ──────────────────────────────────────────────────────────────
    // Returns the normalized category weight [0, 1] — 0 if no category / no data

    private double computeInterestScore(Post post, Map<String, Double> profile) {
        if (post.getCategory() == null || profile.isEmpty())
            return 0.0;
        return profile.getOrDefault(post.getCategory().toUpperCase(), 0.0);
    }

    // ── Location ──────────────────────────────────────────────────────────────
    // Exponential decay: score = e^(-distance / NEAR_RADIUS_KM)
    // 0 km → 1.00 | 50 km → 0.37 | 100 km → 0.14 | 200 km → 0.02
    // Posts with no coordinates score 0 but can still surface via
    // interest/engagement.

    private double computeLocationScore(Post post, Double userLat, Double userLng) {
        if (post.getLatitude() == null || post.getLongitude() == null || userLat == null || userLng == null)
            return 0.0;
        double distKm = haversineKm(userLat, userLng, post.getLatitude(), post.getLongitude());
        return Math.exp(-distKm / NEAR_RADIUS_KM);
    }

    private Double computeDistanceKm(Post post, Double userLat, Double userLng) {
        if (post.getLatitude() == null || post.getLongitude() == null || userLat == null || userLng == null)
            return null;
        return haversineKm(userLat, userLng, post.getLatitude(), post.getLongitude());
    }

    // ── Engagement ────────────────────────────────────────────────────────────
    // Log-normalized so viral posts don't infinitely dominate moderate ones.
    // raw = likes*0.4 + comments*0.6
    // score = log(1 + raw) / log(1 + ENGAGEMENT_CAP) → [0, 1]

    private double computeEngagementScore(Post post,
            Map<Long, Long> likeCounts,
            Map<Long, Long> commentCounts) {
        double likes = likeCounts.getOrDefault(post.getId(), 0L);
        double comments = commentCounts.getOrDefault(post.getId(), 0L);
        double raw = (likes * 0.4) + (comments * 0.6);
        return Math.min(Math.log1p(raw) / Math.log1p(ENGAGEMENT_CAP), 1.0);
    }

    // ── Recency ───────────────────────────────────────────────────────────────
    // Exponential decay with 24-hour half-life.
    // score = 0.5^(hoursOld / RECENCY_HALF_LIFE_HOURS)
    // 0 h → 1.00 | 24 h → 0.50 | 72 h → 0.125 | 7 days → ~0.02

    private double computeRecencyScore(Post post) {
        if (post.getCreatedAt() == null)
            return 0.0;
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
    // SCORE REASON (human-readable label returned to the frontend)
    // ─────────────────────────────────────────────────────────────────────────

    private String buildScoreReason(double interestScore,
            double locationScore,
            Double distanceKm,
            boolean isColdStart) {
        boolean hasInterest = interestScore > 0.05; // >5 % match threshold
        boolean isNear = locationScore > 0.30; // within ~60 km

        String distLabel = distanceKm != null
                ? String.format("%.1f km away", distanceKm)
                : "near you";

        if (isColdStart && isNear)
            return "Popular near you · " + distLabel;
        if (hasInterest && isNear)
            return "Matches your interests · " + distLabel;
        if (hasInterest)
            return "Matches your interests";
        if (isNear)
            return "Near you · " + distLabel;
        return "Popular post";
    }

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getMyPosts(String emailFromToken) {
        User currentUser = userRepository.findByEmail(emailFromToken).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        List<PostResponse> responses = postRepository
                .findActivePostsByUserIdAndType(currentUser.getId(), Post.PostType.COMMUNITY)
                .stream()
                .filter(p -> {
                    String category = p.getCategory() != null ? p.getCategory().toUpperCase() : "";
                    return !"ACTIVITY".equals(category) && !"OFFER".equals(category);
                })
                .map(p -> toResponse(p, null, null))
                .toList();

        List<Long> likedPostIds = likesService.getPostIdsLikedByUser(currentUser.getId());
        List<Long> savedPostIds = savedPostsRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream().map(sp -> sp.getPost().getId()).toList();

        responses.forEach(r -> {
            r.setLikedByCurrentUser(likedPostIds.contains(r.getId()));
            r.setSavedByCurrentUser(savedPostIds.contains(r.getId()));
        });

        return ApiResponse.success("My posts fetched successfully", responses);
    }

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getUserPosts(Long userId) {
        if (!userRepository.existsById(userId)) {
            return ApiResponse.error("User not found");
        }

        List<PostResponse> responses = postRepository
                .findActivePostsByUserIdAndType(userId, Post.PostType.COMMUNITY)
                .stream()
                .filter(p -> {
                    String category = p.getCategory() != null ? p.getCategory().toUpperCase() : "";
                    return !"ACTIVITY".equals(category) && !"OFFER".equals(category);
                })
                .map(p -> toResponse(p, null, null))
                .toList();

        return ApiResponse.success("User posts fetched successfully", responses);
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
        
        r.setActivityId(post.getActivityId()); 
        r.setOfferId(post.getOfferId());       

        if (post.getAuthor() != null) {
            r.setAuthorId(post.getAuthor().getId());
            r.setAuthorEmail(post.getAuthor().getEmail());

            r.setAuthorUsername(post.getAuthor().getUsername());
            r.setAuthorProfileImageUrl(post.getAuthor().getProfileImageUrl());

            String typeStr = post.getType() != null ? post.getType().name() : "";
            String categoryStr = post.getCategory() != null ? post.getCategory().toUpperCase() : "";

            // 1. Populate all possible category-specific fields
            if (post.getOfferId() != null) {
                offerRepo.findById(post.getOfferId()).ifPresent(offer -> {
                    if (offer.getLocation() != null) {
                        r.setLocationId(offer.getLocation().getId());
                        r.setLocationName(offer.getLocation().getName());
                        r.setLocationImageUrl(offer.getLocation().getLogoUrl());
                    }
                });
            } else if ("OFFER".equals(typeStr) || "OFFER".equals(categoryStr)) {
                locationRepo.findByOwnerId(post.getAuthor().getId()).ifPresent(location -> {
                    r.setLocationId(location.getId());
                    r.setLocationName(location.getName());
                    r.setLocationImageUrl(location.getLogoUrl());
                });
            }

            if (post.getActivityId() != null) {
                activityRepository.findById(post.getActivityId()).ifPresent(activity -> {
                    r.setActivityName(activity.getTitle());
                    r.setActivityImageUrl(activity.getImageUrl());
                });
            }

            locationRepo.findByOwnerId(post.getAuthor().getId()).ifPresent(loc -> {
                r.setOwnerId(post.getAuthor().getId());
                r.setOwnerName(loc.getName());
                r.setOwnerImageUrl(loc.getLogoUrl());
            });

            // 2. Override author fields based on the specific CATEGORY requested
            if ("OFFER".equals(categoryStr)) {
                if (r.getLocationName() != null) {
                    r.setAuthorUsername(r.getLocationName());
                    r.setAuthorProfileImageUrl(r.getLocationImageUrl());
                }
            } else if ("ACTIVITY".equals(categoryStr)) {
                if (r.getActivityName() != null && !"USER".equals(typeStr)) {
                    r.setAuthorUsername(r.getActivityName());
                    r.setAuthorProfileImageUrl(r.getActivityImageUrl());
                }
            } else if ("OWNER".equals(categoryStr)) {
                if (r.getOwnerName() != null) {
                    r.setAuthorUsername(r.getOwnerName());
                    r.setAuthorProfileImageUrl(r.getOwnerImageUrl());
                }
            }
        }

        r.setLikeCount(likesService.countByPostId(post.getId()).getData());
        r.setCommentCount(commentsService.countByPostId(post.getId()).getData());
        return r;
    }

    @Override
    @Transactional
    public ApiResponse<List<PostResponse>> getPostsByActivityId(Long activityId) {
        List<PostResponse> responses = postRepository
                .findActivePostsByActivityId(activityId)
                .stream()
                .map(p -> toResponse(p, null, null))
                .toList();

        return ApiResponse.success("Activity posts fetched successfully", responses);
    }
    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL RECORD
    // ─────────────────────────────────────────────────────────────────────────

    private record ScoredPost(
            Post post,
            double score,
            Double distanceKm,
            String scoreReason) {
    }
}