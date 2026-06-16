package com.jomap.backend.Services.Fav;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Fav.FavoriteEventDto;
import com.jomap.backend.DTOs.Fav.FavoriteLocationDto;
import com.jomap.backend.DTOs.Fav.FavoritePostDto;
import com.jomap.backend.DTOs.Fav.FavoritesDataDto;
import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivitySchedule;
import com.jomap.backend.Entities.Governorate.Place;
import com.jomap.backend.Entities.Governorate.PlaceRepository;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPost;
import com.jomap.backend.Entities.Posts.SavedPosts.SavedPostsRepository;
import com.jomap.backend.Entities.Posts.postComments.PostCommentRepository;
import com.jomap.backend.Entities.Posts.postLikes.PostLikesRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FavoritesServiceImpl implements FavoritesService {

    private final UserRepository userRepository;
    private final LocationRepo locationRepository;
    private final PlaceRepository placeRepository;
    private final ActivityRepository activityRepository;
    private final SavedPostsRepository savedPostsRepository;
    private final PostLikesRepository postLikesRepository;
    private final PostCommentRepository postCommentRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<FavoritesDataDto> getMyFavorites(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        User user = userOptional.get();
        FavoritesDataDto data = new FavoritesDataDto();

        List<FavoriteLocationDto> locations = new ArrayList<>();
        if (user.getFavoriteLocations() != null) {
            locations.addAll(user.getFavoriteLocations().stream()
                    .map(this::mapLocationToDto)
                    .toList());
        }
        if (user.getFavoritePlaces() != null) {
            locations.addAll(user.getFavoritePlaces().stream()
                    .map(this::mapPlaceToDto)
                    .toList());
        }

        List<FavoriteEventDto> events = user.getFavoriteEvents() == null
                ? Collections.emptyList()
                : user.getFavoriteEvents().stream()
                        .map(this::mapActivityToDto)
                        .toList();

        data.setLocations(locations);
        data.setEvents(events);
        List<FavoritePostDto> posts = savedPostsRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(SavedPost::getPost)
                .filter(post -> post != null && !Boolean.TRUE.equals(post.getIsDeleted()))
                .map(this::mapPostToDto)
                .toList();

        data.setPosts(posts);

        return ApiResponse.success("تم جلب المحفوظات بنجاح", data);
    }

    @Override
    @Transactional
    public ApiResponse<Void> addFavoriteLocation(Long locationId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) {
            return ApiResponse.error("الموقع غير موجود");
        }

        User user = userOptional.get();
        LocationList location = locationOptional.get();

        boolean alreadyExists = user.getFavoriteLocations().stream()
                .anyMatch(loc -> loc.getId() != null && loc.getId().equals(locationId));

        if (alreadyExists) {
            return ApiResponse.success("الموقع موجود بالفعل ضمن المفضلة", null);
        }

        user.getFavoriteLocations().add(location);
        userRepository.save(user);
        return ApiResponse.success("تمت إضافة الموقع إلى المفضلة بنجاح", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeFavoriteLocation(Long locationId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) {
            return ApiResponse.error("الموقع غير موجود");
        }

        User user = userOptional.get();
        LocationList location = locationOptional.get();

        boolean removed = user.getFavoriteLocations().removeIf(
                loc -> loc.getId() != null && loc.getId().equals(locationId)
        );

        if (!removed) {
            return ApiResponse.success("الموقع غير موجود ضمن المفضلة", null);
        }
        userRepository.save(user);
        return ApiResponse.success("تمت إزالة الموقع من المفضلة بنجاح", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> addFavoritePlace(Long placeId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<Place> placeOptional = placeRepository.findById(placeId);
        if (placeOptional.isEmpty()) {
            return ApiResponse.error("المعلم غير موجود");
        }

        User user = userOptional.get();
        Place place = placeOptional.get();

        boolean alreadyExists = user.getFavoritePlaces().stream()
                .anyMatch(savedPlace -> savedPlace.getId() != null && savedPlace.getId().equals(placeId));

        if (alreadyExists) {
            return ApiResponse.success("المعلم موجود بالفعل ضمن المفضلة", null);
        }

        user.getFavoritePlaces().add(place);
        userRepository.save(user);
        return ApiResponse.success("تمت إضافة المعلم إلى المفضلة بنجاح", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeFavoritePlace(Long placeId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<Place> placeOptional = placeRepository.findById(placeId);
        if (placeOptional.isEmpty()) {
            return ApiResponse.error("المعلم غير موجود");
        }

        User user = userOptional.get();
        Place place = placeOptional.get();

        boolean removed = user.getFavoritePlaces().removeIf(
                savedPlace -> savedPlace.getId() != null && savedPlace.getId().equals(placeId)
        );

        if (!removed) {
            return ApiResponse.success("المعلم غير موجود ضمن المفضلة", null);
        }
        userRepository.save(user);
        return ApiResponse.success("تمت إزالة المعلم من المفضلة بنجاح", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> addFavoriteActivity(Long activityId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            return ApiResponse.error("الفعالية غير موجودة");
        }

        User user = userOptional.get();
        Activity activity = activityOptional.get();

        boolean alreadyExists = user.getFavoriteEvents().stream()
                .anyMatch(event -> event.getId() != null && event.getId().equals(activityId));

        if (alreadyExists) {
            return ApiResponse.success("الفعالية موجودة بالفعل ضمن المفضلة", null);
        }

        user.getFavoriteEvents().add(activity);
        userRepository.save(user);
        return ApiResponse.success("تمت إضافة الفعالية إلى المفضلة بنجاح", null);
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeFavoriteActivity(Long activityId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }

        Optional<Activity> activityOptional = activityRepository.findById(activityId);
        if (activityOptional.isEmpty()) {
            return ApiResponse.error("الفعالية غير موجودة");
        }

        User user = userOptional.get();
        Activity activity = activityOptional.get();

        boolean removed = user.getFavoriteEvents().removeIf(
                event -> event.getId() != null && event.getId().equals(activityId)
        );

        if (!removed) {
            return ApiResponse.success("الفعالية غير موجودة ضمن المفضلة", null);
        }
        userRepository.save(user);
        return ApiResponse.success("تمت إزالة الفعالية من المفضلة بنجاح", null);
    }

    private FavoriteLocationDto mapLocationToDto(LocationList location) {
        FavoriteLocationDto dto = new FavoriteLocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setCategory(location.getCategory() != null ? location.getCategory().name() : null);
        dto.setDescription(location.getDescription());
        dto.setImageUrl(location.getLogoUrl()); //imageUrl is the logo
        dto.setCoverUrl(location.getCoverUrl()); //coverUrl is the cover URL
        dto.setRating(location.getRating());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setPlaceType("LOCATION");

        // الحقول الإضافية البريميوم
        dto.setGovernorateName(location.getGovernorate() != null ? location.getGovernorate().getName() : null);
        dto.setIsOpenNow(calculateIsOpenNow(location));
        dto.setReviewCount(location.getReviewCount() != null ? location.getReviewCount() : 0);
        dto.setLocationType(location.getCategory() != null ? location.getCategory().name() : null);

        return dto;
    }

    private FavoriteLocationDto mapPlaceToDto(Place place) {
        FavoriteLocationDto dto = new FavoriteLocationDto();
        dto.setId(place.getId());
        dto.setName(place.getName());
        dto.setCategory(place.getCategory() != null ? place.getCategory().name() : null);
        dto.setDescription(place.getDescription());
        dto.setImageUrl(place.getImageUrl());
        dto.setCoverUrl(null);
        dto.setRating(null);
        dto.setLatitude(null);
        dto.setLongitude(null);
        dto.setPlaceType("PLACE");

        // الحقول الإضافية للـ Place للتوافقية
        dto.setGovernorateName(place.getGovernorate() != null ? place.getGovernorate().getName() : null);
        dto.setIsOpenNow(null);
        dto.setReviewCount(0);
        dto.setLocationType(place.getCategory() != null ? place.getCategory().name() : null);

        return dto;
    }

    private boolean calculateIsOpenNow(LocationList location) {
        boolean isOpenNow = false;
        String currentDayName = getCurrentArabicDayName();
        java.time.LocalTime now = java.time.LocalTime.now();

        if (location.getSchedules() != null) {
            for (com.jomap.backend.Entities.Locations.LocationSchedule s : location.getSchedules()) {
                if (s.getDayName() != null && s.getDayName().equals(currentDayName)) {
                    if (Boolean.TRUE.equals(s.getIsClosed())) {
                        isOpenNow = false;
                    } else if (s.getStartTime() != null && s.getEndTime() != null) {
                        try {
                            java.time.LocalTime start = java.time.LocalTime.parse(s.getStartTime());
                            java.time.LocalTime end = java.time.LocalTime.parse(s.getEndTime());
                            if (start.isBefore(end)) {
                                isOpenNow = !now.isBefore(start) && !now.isAfter(end);
                            } else {
                                isOpenNow = !now.isBefore(start) || !now.isAfter(end);
                            }
                        } catch (Exception e) {
                            isOpenNow = false;
                        }
                    }
                }
            }
        }
        return isOpenNow;
    }

    private String getCurrentArabicDayName() {
        java.time.DayOfWeek day = java.time.LocalDate.now().getDayOfWeek();
        switch (day) {
            case MONDAY: return "الإثنين";
            case TUESDAY: return "الثلاثاء";
            case WEDNESDAY: return "الأربعاء";
            case THURSDAY: return "الخميس";
            case FRIDAY: return "الجمعة";
            case SATURDAY: return "السبت";
            case SUNDAY: return "الأحد";
            default: return "";
        }
    }

    private FavoriteEventDto mapActivityToDto(Activity activity) {
        FavoriteEventDto dto = new FavoriteEventDto();
        dto.setId(activity.getId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setLocationName(activity.getActivityLocation());
        dto.setImageUrl(activity.getImageUrl());
        dto.setOrganizer(activity.getCreatedBy() != null ? activity.getCreatedBy().getUsername() : null);
        dto.setAttendeesCount(activity.getAttendeesCount() != null ? activity.getAttendeesCount() : 0);
        dto.setPrice(activity.getPrice() != null ? String.valueOf(activity.getPrice()) : null);

        ActivitySchedule firstSchedule = extractFirstSchedule(activity);
        if (firstSchedule != null) {
            dto.setDate(firstSchedule.getDate());
            dto.setTime(firstSchedule.getStartTime());
        }

        return dto;
    }

    private FavoritePostDto mapPostToDto(Post post) {
        FavoritePostDto dto = new FavoritePostDto();
        dto.setId(post.getId());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getMediaUrl());
        dto.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
        dto.setCategory(post.getCategory());
        dto.setLikeCount(Math.toIntExact(postLikesRepository.countByPostId(post.getId())));
        dto.setCommentCount(Math.toIntExact(postCommentRepository.countActiveByPostId(post.getId())));

        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorName(post.getAuthor().getUsername());
            dto.setAuthorAvatarUrl(post.getAuthor().getProfileImageUrl());
        }

        return dto;
    }

    private ActivitySchedule extractFirstSchedule(Activity activity) {
        if (activity.getSchedules() == null || activity.getSchedules().isEmpty()) {
            return null;
        }
        return activity.getSchedules().get(0);
    }
}
