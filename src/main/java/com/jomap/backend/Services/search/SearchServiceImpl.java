package com.jomap.backend.Services.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.search.SearchItem;
import com.jomap.backend.DTOs.search.SearchType;
import com.jomap.backend.Entities.Activities.Activity;
import com.jomap.backend.Entities.Activities.ActivityRepository;
import com.jomap.backend.Entities.Activities.ActivitySchedule;
import com.jomap.backend.Entities.Activities.ActivityStatus;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Users.Profile.UserProfile;
import com.jomap.backend.Entities.Users.Profile.UserProfileRepository;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Entities.Friendship.Friendship;
import com.jomap.backend.Entities.Friendship.FriendshipRepository;
import com.jomap.backend.Entities.Friendship.FriendshipStatus;
import com.jomap.backend.Entities.Offers.Offer;
import com.jomap.backend.Entities.Offers.OfferRepo;
import com.jomap.backend.Entities.Offers.OfferStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int SEARCH_LIMIT = 10;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ActivityRepository activityRepository;
    private final LocationRepo locationRepository;
    private final FriendshipRepository friendshipRepository;
    private final OfferRepo offerRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<com.jomap.backend.DTOs.PaginatedResponse<SearchItem>> getAllItems(String userEmail, int page, int size) {
        List<SearchItem> items = new ArrayList<>();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        User currentUser = null;
        if (userEmail != null) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
        }
        final User finalCurrentUser = currentUser;

        org.springframework.data.domain.Page<User> usersPage = userRepository.findByIsActiveTrueAndRoleNotOrderByIdDesc(Role.ADMIN, pageable);
        usersPage.getContent().stream()
                .map(u -> toUserSearchItem(u, finalCurrentUser))
                .forEach(items::add);

        org.springframework.data.domain.Page<Activity> activitiesPage = activityRepository.findByStatusInOrderByIdDesc(List.of(ActivityStatus.APPROVED, ActivityStatus.POSTPONED), pageable);
        activitiesPage.getContent().stream()
                .map(act -> toEventSearchItem(act, finalCurrentUser))
                .forEach(items::add);

        org.springframework.data.domain.Page<LocationList> locationsPage = locationRepository.findByActiveTrueAndStatusOrderByIdDesc(com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED, pageable);
        locationsPage.getContent().stream()
                .map(loc -> toLocationSearchItem(loc, finalCurrentUser))
                .forEach(items::add);

        org.springframework.data.domain.Page<Offer> offersPage = offerRepository.findByStatusOrderByIdDesc(OfferStatus.ACTIVE, pageable);
        offersPage.getContent().stream()
                .map(off -> toOfferSearchItem(off, finalCurrentUser))
                .forEach(items::add);

        long totalElements = usersPage.getTotalElements() + activitiesPage.getTotalElements() + locationsPage.getTotalElements() + offersPage.getTotalElements();
        int totalPages = Math.max(Math.max(usersPage.getTotalPages(), activitiesPage.getTotalPages()), Math.max(locationsPage.getTotalPages(), offersPage.getTotalPages()));
        boolean last = page >= totalPages - 1 || totalPages == 0;

        com.jomap.backend.DTOs.PaginatedResponse<SearchItem> paginatedResponse = com.jomap.backend.DTOs.PaginatedResponse.<SearchItem>builder()
                .content(items)
                .pageNo(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(last)
                .build();

        return ApiResponse.success("Items retrieved successfully", paginatedResponse);
    }

    private SearchItem toUserSearchItem(User user, User currentUser) {
        SearchItem item = new SearchItem();
        item.setId(user.getId());
        item.setType(SearchType.USER);

        Optional<UserProfile> profile = userProfileRepository.findByUserId(user.getId());
        if (profile.isPresent()) {
            UserProfile p = profile.get();
            item.setTitle(trimJoin(p.getFirstName(), p.getLastName()));
            item.setSubTitle(firstNonBlank(p.getBio(), p.getLocation(), user.getUsername()));
            item.setImageUrl(p.getProfileImageUrl());
            item.setLocationName(p.getLocation());
        } else {
            item.setTitle(user.getUsername());
            item.setSubTitle(user.getEmail());
            item.setImageUrl(user.getProfileImageUrl());
        }

        item.setImageRes(0);
        item.setFriendshipStatus("NONE");

        if (currentUser != null && !currentUser.getId().equals(user.getId())) {
            List<Friendship> friendships = friendshipRepository
                    .findByRequesterAndReceiverOrRequesterAndReceiver(currentUser, user, user, currentUser);
            
            if (!friendships.isEmpty()) {
                Friendship f = friendships.get(friendships.size() - 1); // Get the latest one
                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    item.setFriendshipStatus("ACCEPTED");
                } else if (f.getStatus() == FriendshipStatus.PENDING) {
                    if (f.getRequester().getId().equals(currentUser.getId())) {
                        item.setFriendshipStatus("PENDING_SENT");
                    } else {
                        item.setFriendshipStatus("PENDING_RECEIVED");
                    }
                }
            }
        }

        return item;
    }

    private SearchItem toEventSearchItem(Activity activity, User currentUser) {
        SearchItem item = new SearchItem();
        item.setId(activity.getId());
        item.setType(SearchType.ACTIVITY);
        item.setTitle(activity.getTitle());
        item.setDescription(activity.getDescription());
        // subTitle: نمرر فيه اسم المنظم الفعلي createdByUsername ليظهر بقمة الكارت كـ Organizer
        item.setSubTitle(activity.getCreatedBy() != null ? activity.getCreatedBy().getUsername() : null);
        
        // locationName = اسم المنشأة المنظِّمة (للرابط الأزرق أعلى الكارد)
        if (activity.getLocationId() != null) {
            locationRepository.findById(activity.getLocationId())
                .ifPresent(loc -> item.setLocationName(loc.getName()));
        }
        item.setImageUrl(activity.getImageUrl());
        item.setImageRes(0);

        if (activity.getGovernorate() != null) {
            item.setGovernorateName(activity.getGovernorate().getName());
        }

        boolean isFavorite = false;
        if (currentUser != null && currentUser.getFavoriteEvents() != null) {
            isFavorite = currentUser.getFavoriteEvents().stream()
                    .anyMatch(a -> a.getId().equals(activity.getId()));
        }
        item.setIsFavorite(isFavorite);

        if (activity.getSchedules() != null && !activity.getSchedules().isEmpty()) {
            ActivitySchedule firstSchedule = activity.getSchedules().get(0);
            item.setActivityDate(firstSchedule.getDate());
            item.setStartDate(firstSchedule.getDate());
            item.setEndDate(activity.getSchedules().get(activity.getSchedules().size() - 1).getDate());
            
            item.setSchedules(activity.getSchedules().stream()
                .map(s -> new com.jomap.backend.DTOs.Activities.ActivitySchedule(s.getDate(), s.getDayName(), s.getStartTime(), s.getEndTime()))
                .collect(java.util.stream.Collectors.toList()));
        } else {
            item.setSchedules(new java.util.ArrayList<>());
        }

        item.setPrice(activity.getPrice());
        item.setActivityLocation(activity.getActivityLocation());
        item.setItemsCount(activity.getAttendeesCount() != null ? activity.getAttendeesCount() : 0);
        item.setLocationId(activity.getLocationId());
        
        if (activity.getStatus() != null) {
            item.setStatus(activity.getStatus().getLabel());
        }

        return item;
    }

    private SearchItem toLocationSearchItem(LocationList location, User currentUser) {
        SearchItem item = new SearchItem();
        item.setId(location.getId());
        if (location.getCategory() == com.jomap.backend.Entities.Locations.LocationCategory.ORGANIZATION || 
            location.getCategory() == com.jomap.backend.Entities.Locations.LocationCategory.VOLUNTEER_TEAM) {
            item.setType(SearchType.ORGANIZATION);
        } else {
            item.setType(SearchType.LOCATION);
        }
        item.setTitle(location.getName());
        item.setSubTitle(firstNonBlank(location.getDescription(), categoryLabel(location)));
        item.setLocationName(location.getName());
        item.setRating(location.getRating());
        item.setImageUrl(location.getLogoUrl());
        item.setImageRes(0);
        item.setCategory(categoryLabel(location));
        
        if (location.getGovernorate() != null) {
            item.setGovernorateName(location.getGovernorate().getName());
        }
        
        item.setCoverUrl(location.getCoverUrl());
        item.setReviewCount(location.getReviewCount());

        boolean isFavorite = false;
        if (currentUser != null && currentUser.getFavoriteLocations() != null) {
            isFavorite = currentUser.getFavoriteLocations().stream()
                    .anyMatch(l -> l.getId().equals(location.getId()));
        }
        item.setIsFavorite(isFavorite);

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
        item.setIsOpenNow(isOpenNow);

        if (location.getStatus() != null) {
            item.setStatus(location.getStatus().getLabel());
        }

        return item;
    }

    private String trimJoin(String first, String last) {
        String fullName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
        return fullName.isEmpty() ? null : fullName;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private String categoryLabel(LocationList location) {
        return location.getCategory() != null ? location.getCategory().name() : null;
    }

    private SearchItem toOfferSearchItem(Offer offer, User currentUser) {
        SearchItem item = new SearchItem();
        item.setId(offer.getId());
        item.setType(SearchType.OFFER);
        item.setTitle(offer.getTitle());
        item.setSubTitle(offer.getDescription());
        item.setDescription(offer.getDescription());
        item.setImageUrl(offer.getImageUrl());
        item.setImageRes(0);
        item.setViewsCount(offer.getViewsCount() != null ? offer.getViewsCount() : 0);
        item.setClicksCount(offer.getClicksCount() != null ? offer.getClicksCount() : 0);
        
        if (offer.getLocation() != null) {
            item.setLocationName(offer.getLocation().getName());
            item.setLocationId(offer.getLocation().getId());
        }

        if (offer.getGovernorate() != null) {
            item.setGovernorateName(offer.getGovernorate().getName());
        }

        boolean isFavorite = false;
        if (currentUser != null && currentUser.getFavoriteOffers() != null) {
            isFavorite = currentUser.getFavoriteOffers().stream()
                    .anyMatch(o -> o.getId().equals(offer.getId()));
        }
        item.setIsFavorite(isFavorite);

        String sDate = offer.getStartDate();
        String eDate = offer.getEndDate();
        item.setStartDate(sDate);
        item.setEndDate(eDate);
        
        if (offer.getProducts() != null) {
            item.setItemsCount(offer.getProducts().size());
            item.setOfferProducts(offer.getProducts().stream()
                .map(p -> new com.jomap.backend.DTOs.Offers.OfferProductResponse(p.getId(), p.getProductName(), p.getPriceBefore(), p.getPriceAfter()))
                .collect(java.util.stream.Collectors.toList()));
        } else {
            item.setItemsCount(0);
            item.setOfferProducts(new java.util.ArrayList<>());
        }
        
        if (offer.getStatus() != null) {
            item.setStatus(offer.getStatus().getLabel());
        }

        if (sDate != null && !sDate.isBlank()) {
            item.setActivityDate(sDate + (eDate != null && !eDate.isBlank() ? " - " + eDate : ""));
        } else if (eDate != null && !eDate.isBlank()) {
            item.setActivityDate(eDate);
        }

        return item;
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
}
