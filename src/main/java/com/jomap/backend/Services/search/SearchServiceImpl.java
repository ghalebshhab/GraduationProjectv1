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
    public ApiResponse<List<SearchItem>> getAllItems(String userEmail) {
        List<SearchItem> items = new ArrayList<>(SEARCH_LIMIT * 3);

        User currentUser = null;
        if (userEmail != null) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
        }
        final User finalCurrentUser = currentUser;

        userRepository.findTop10ByIsActiveTrueAndRoleNotOrderByIdDesc(Role.ADMIN)
                .stream()
                .map(u -> toUserSearchItem(u, finalCurrentUser))
                .forEach(items::add);

        activityRepository.findTop10ByStatusInOrderByIdDesc(List.of(ActivityStatus.APPROVED, ActivityStatus.POSTPONED))
                .stream()
                .map(this::toEventSearchItem)
                .forEach(items::add);

        locationRepository.findTop10ByActiveTrueOrderByIdDesc()
                .stream()
                .map(this::toLocationSearchItem)
                .forEach(items::add);

        offerRepository.findTop10ByStatusOrderByIdDesc(OfferStatus.ACTIVE)
                .stream()
                .map(this::toOfferSearchItem)
                .forEach(items::add);

        return ApiResponse.success("Items retrieved successfully", items);
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

    private SearchItem toEventSearchItem(Activity activity) {
        SearchItem item = new SearchItem();
        item.setId(activity.getId());
        item.setType(SearchType.EVENT);
        item.setTitle(activity.getTitle());
        item.setSubTitle(firstNonBlank(activity.getActivityLocation(), activity.getDescription()));
        item.setLocationName(activity.getActivityLocation());
        item.setImageUrl(activity.getImageUrl());
        item.setImageRes(0);

        if (activity.getSchedules() != null && !activity.getSchedules().isEmpty()) {
            ActivitySchedule firstSchedule = activity.getSchedules().get(0);
            item.setEventDate(firstSchedule.getDate());
        }

        return item;
    }

    private SearchItem toLocationSearchItem(LocationList location) {
        SearchItem item = new SearchItem();
        item.setId(location.getId());
        item.setType(SearchType.LOCATION);
        item.setTitle(location.getName());
        item.setSubTitle(firstNonBlank(location.getDescription(), categoryLabel(location)));
        item.setLocationName(location.getName());
        item.setRating(location.getRating());
        item.setImageUrl(location.getCoverUrl() != null ? location.getCoverUrl() : location.getLogoUrl());
        item.setImageRes(0);
        item.setCategory(categoryLabel(location));
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

    private SearchItem toOfferSearchItem(Offer offer) {
        SearchItem item = new SearchItem();
        item.setId(offer.getId());
        item.setType(SearchType.OFFER);
        item.setTitle(offer.getTitle());
        item.setSubTitle(offer.getDescription());
        item.setImageUrl(offer.getImageUrl());
        item.setImageRes(0);
        
        if (offer.getLocation() != null) {
            item.setLocationName(offer.getLocation().getName());
        }

        String sDate = offer.getStartDate();
        String eDate = offer.getEndDate();
        if (sDate != null && !sDate.isBlank()) {
            item.setEventDate(sDate + (eDate != null && !eDate.isBlank() ? " - " + eDate : ""));
        } else if (eDate != null && !eDate.isBlank()) {
            item.setEventDate(eDate);
        }

        return item;
    }
}
