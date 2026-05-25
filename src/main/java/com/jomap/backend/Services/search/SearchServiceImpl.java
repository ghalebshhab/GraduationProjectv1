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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int SEARCH_LIMIT = 10;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ActivityRepository activityRepository;
    private final LocationRepo locationRepository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<SearchItem>> getAllItems() {
        List<SearchItem> items = new ArrayList<>(SEARCH_LIMIT * 3);

        userRepository.findTop10ByIsActiveTrueAndRoleNotOrderByIdDesc(Role.ADMIN)
                .stream()
                .map(this::toUserSearchItem)
                .forEach(items::add);

        activityRepository.findTop10ByStatusOrderByIdDesc(ActivityStatus.APPROVED)
                .stream()
                .map(this::toEventSearchItem)
                .forEach(items::add);

        locationRepository.findTop10ByActiveTrueAndApprovedTrueOrderByIdDesc()
                .stream()
                .map(this::toLocationSearchItem)
                .forEach(items::add);

        return ApiResponse.success("Items retrieved successfully", items);
    }

    private SearchItem toUserSearchItem(User user) {
        SearchItem item = new SearchItem();
        item.setId(user.getId());
        item.setType(SearchType.USER);

        Optional<UserProfile> profile = userProfileRepository.findByUserId(user.getId());
        if (profile.isPresent()) {
            UserProfile p = profile.get();
            item.setTitle(trimJoin(p.getFirstName(), p.getLastName()));
            item.setSubTitle(firstNonBlank(p.getBio(), p.getLocation(), user.getUsername()));
        } else {
            item.setTitle(user.getUsername());
            item.setSubTitle(user.getEmail());
        }

        item.setImageRes(0);
        return item;
    }

    private SearchItem toEventSearchItem(Activity activity) {
        SearchItem item = new SearchItem();
        item.setId(activity.getId());
        item.setType(SearchType.EVENT);
        item.setTitle(activity.getTitle());
        item.setSubTitle(firstNonBlank(activity.getActivityLocation(), activity.getDescription()));
        item.setLocationName(activity.getActivityLocation());
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
        item.setImageRes(0);
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
}
