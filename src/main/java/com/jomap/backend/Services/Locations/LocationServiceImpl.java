package com.jomap.backend.Services.Locations;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.CreateLocationRequest;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Locations.UpdateLocationRequest;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Governorate.Governorate;
import com.jomap.backend.Entities.Governorate.GovernorateRepository;
import com.jomap.backend.Entities.Locations.LocationCategory;
import com.jomap.backend.Entities.Locations.LocationStatus;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Auth.JwtService;
import com.jomap.backend.Entities.Locations.LocationBlock;
import com.jomap.backend.Entities.Locations.LocationBlockRepository;
import com.jomap.backend.Entities.Locations.LocationVisit;
import com.jomap.backend.Entities.Locations.LocationVisitRepository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepo locationRepository;
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;
    private final JwtService jwtService;
    private final LocationBlockRepository locationBlockRepository;
    private final LocationVisitRepository locationVisitRepository;

    @Override
    @Transactional
    public ApiResponse<LocationResponse> createLocation(CreateLocationRequest request, String currentUserEmail) {
        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
        if (!userResponse.isSuccess())
            return ApiResponse.error(userResponse.getMessage());

        User owner = userResponse.getData();

        if (request.getGovernorateId() == null) {
            return ApiResponse.error("يجب اختيار المحافظة");
        }

        ApiResponse<Void> validationResponse = validateCreateRequest(request);
        if (!validationResponse.isSuccess())
            return ApiResponse.error(validationResponse.getMessage());

        if (locationRepository.existsByOwnerId(owner.getId())) {
            return ApiResponse.error("لديك موقع مسجل بالفعل، يمكنك تعديل موقعك الحالي.");
        }

        LocationList location = new LocationList();
        mapRequestToEntity(location, request);

        return governorateRepository.findById(request.getGovernorateId())
                .map(gov -> {
                    location.setGovernorate(gov);
                    location.setOwner(owner);

                    location.setStatus(LocationStatus.PENDING);
                    location.setApproved(false);
                    location.setActive(true);

                    LocationList savedLocation = locationRepository.save(location);

                    owner.setRole(Role.OWNER);
                    userRepository.save(owner);

                    String newToken = jwtService.generateToken(owner.getEmail());

                    LocationResponse response = mapToResponse(savedLocation);
                    response.setNewToken(newToken);

                    return ApiResponse.success(
                            "تم إنشاء الموقع بنجاح، بانتظار موافقة المسؤول.",
                            response);
                }).orElse(ApiResponse.error("المحافظة المحددة غير موجودة في النظام"));
    }

    @Override
    @Transactional
    public ApiResponse<LocationResponse> updateLocation(Long locationId, UpdateLocationRequest request,
            String currentUserEmail) {
        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
        if (!userResponse.isSuccess())
            return ApiResponse.error(userResponse.getMessage());

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty())
            return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();

        if (!location.getOwner().getId().equals(userResponse.getData().getId())) {
            return ApiResponse.error("ليس لديك صلاحية لتعديل هذا الموقع");
        }

        if (request.getCategory() != null) {
            LocationCategory currentCategory = location.getCategory();
            LocationCategory newCategory;
            try {
                newCategory = LocationCategory.valueOf(request.getCategory().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("فئة الموقع المحددة غير صالحة");
            }

            boolean isCurrentVolOrOrg = (currentCategory == LocationCategory.VOLUNTEER_TEAM || currentCategory == LocationCategory.ORGANIZATION);
            boolean isNewVolOrOrg = (newCategory == LocationCategory.VOLUNTEER_TEAM || newCategory == LocationCategory.ORGANIZATION);

            if (isCurrentVolOrOrg && !isNewVolOrOrg) {
                return ApiResponse.error("غير مسموح بالتحويل بين الفئات التطوعية والفئات التجارية");
            }
            if (!isCurrentVolOrOrg && isNewVolOrOrg) {
                return ApiResponse.error("غير مسموح بالتحويل بين الفئات التطوعية والفئات التجارية");
            }
        }

        if (request.getGovernorateId() != null) {
            Optional<Governorate> govOpt = governorateRepository.findById(request.getGovernorateId());
            if (govOpt.isEmpty())
                return ApiResponse.error("المحافظة المحددة غير مدعومة");
            location.setGovernorate(govOpt.get());
        }

        updateEntityFields(location, request);

        if (location.getStatus() == LocationStatus.REJECTED) {
            location.setStatus(LocationStatus.PENDING);
            location.setApproved(false);
            location.setRejectionReason(null);
        } else if (location.getStatus() != LocationStatus.PUBLISHED && location.getStatus() != LocationStatus.APPROVED) {
            location.setStatus(LocationStatus.PENDING);
            location.setApproved(false);
        }

        return ApiResponse.success("تم تحديث البيانات، بانتظار مراجعة المسؤول واعتمادها مجدداً.",
                mapToResponse(locationRepository.save(location)));
    }

    @Override
    @Transactional
    public ApiResponse<LocationResponse> approveLocation(Long locationId) {
        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty())
            return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();

        location.setStatus(LocationStatus.PUBLISHED);
        location.setApproved(true);
        location.setActive(true);

        return ApiResponse.success("تمت الموافقة ونشر الموقع بنجاح.", mapToResponse(locationRepository.save(location)));
    }

    @Override
    @Transactional
    public ApiResponse<LocationResponse> changeLocationStatus(Long id, String status, String currentUserEmail) {
        try {
            ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
            if (!userResponse.isSuccess())
                return ApiResponse.error(userResponse.getMessage());

            java.util.Optional<LocationList> locationOpt = locationRepository.findById(id);
            if (locationOpt.isEmpty()) {
                return ApiResponse.error("الموقع غير موجود");
            }
            LocationList location = locationOpt.get();

            if (!location.getOwner().getId().equals(userResponse.getData().getId())) {
                return ApiResponse.error("ليس لديك صلاحية لتعديل حالة هذا الموقع");
            }

            LocationStatus currentStatus = location.getStatus();
            LocationStatus newStatus;

            if ("RESTORE".equalsIgnoreCase(status)) {
                if (currentStatus != LocationStatus.DELETED && currentStatus != LocationStatus.DEACTIVATED) {
                    return ApiResponse.error("لا يمكن استرجاع المنشأة لأنها ليست في حالة الحذف أو التعطيل");
                }
                
                if (currentStatus == LocationStatus.DEACTIVATED) {
                    newStatus = LocationStatus.PUBLISHED;
                } else {
                    newStatus = location.getPreviousStatus() != null ? location.getPreviousStatus() : LocationStatus.PUBLISHED;
                }
            } else {
                newStatus = LocationStatus.valueOf(status.toUpperCase());
            }

            if (newStatus == LocationStatus.REJECTED) {
                return ApiResponse.error("رفض المنشأة يجب أن يتم من لوحة تحكم الأدمن مع إدخال سبب الرفض حتى يتم حفظ السبب وإرسال إشعار للمالك.");
            }

            if (newStatus == LocationStatus.PUBLISHED) {
                if (currentStatus != LocationStatus.APPROVED && currentStatus != LocationStatus.DEACTIVATED && currentStatus != LocationStatus.DELETED) {
                    return ApiResponse.error("لا يمكن نشر المنشأة إلا إذا كانت حالتها مقبولة أو معطلة مؤقتاً أو محذوفة ومؤجلة الحذف");
                }
            } else if (newStatus == LocationStatus.DEACTIVATED) {
                if (currentStatus != LocationStatus.PUBLISHED) {
                    return ApiResponse.error("لا يمكن تعطيل المنشأة إلا إذا كانت منشورة");
                }
            } else if (newStatus == LocationStatus.DELETED) {
                if (currentStatus != LocationStatus.DELETED) {
                    location.setPreviousStatus(currentStatus);
                }
            }

            location.setStatus(newStatus);

            if (newStatus == LocationStatus.DELETED) {
                location.setDeletedAt(LocalDateTime.now());
                location.setApproved(false);
                location.setActive(false);
            } else if (newStatus == LocationStatus.PUBLISHED) {
                location.setDeletedAt(null);
                location.setPreviousStatus(null);
                location.setApproved(true);
                location.setActive(true);
            } else if (newStatus == LocationStatus.APPROVED) {
                location.setDeletedAt(null);
                location.setPreviousStatus(null);
                location.setApproved(true);
                location.setActive(false);
            } else {
                location.setDeletedAt(null);
                location.setPreviousStatus(null);
                location.setApproved(false);
                location.setActive(false);
            }

            String successMsg = "تم تحديث حالة الموقع بنجاح.";
            if ("RESTORE".equalsIgnoreCase(status)) {
                successMsg = "تم استرجاع المنشأة للحالة السابقة بنجاح.";
            } else if (newStatus == LocationStatus.DEACTIVATED) {
                successMsg = "تم إيقاف نشاط الموقع مؤقتاً بنجاح.";
            } else if (newStatus == LocationStatus.DELETED) {
                successMsg = "تم جدولة حذف المنشأة نهائياً من الأنظمة، لديك 24 ساعة للتراجع عن القرار.";
            }

            return ApiResponse.success(successMsg, mapToResponse(locationRepository.save(location)));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error("حالة الموقع المرسلة غير صالحة بالنظام");
        } catch (Exception e) {
            return ApiResponse.error("حدث خطأ أثناء معالجة الطلب: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<LocationResponse> getLocationById(Long locationId) {
        return locationRepository.findById(locationId)
                .map(loc -> ApiResponse.success("Fetched", mapToResponse(loc)))
                .orElse(ApiResponse.error("Not found"));
    }

    @Override
    public ApiResponse<List<LocationResponse>> getLocations(Long governorateId, LocationCategory category) {
        List<LocationList> locations = locationRepository.findByActiveTrueAndApprovedTrue();
        return ApiResponse.success("Success", locations.stream().map(this::mapToResponse).toList());
    }

    @Override
    public ApiResponse<LocationResponse> getMyLocation(String currentUserEmail) {
        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
        if (!userResponse.isSuccess())
            return ApiResponse.error(userResponse.getMessage());

        return locationRepository.findByOwnerId(userResponse.getData().getId())
                .map(loc -> ApiResponse.success("My location", mapToResponse(loc)))
                .orElse(ApiResponse.error("No location found"));
    }

    @Override
    @Transactional
    public ApiResponse<String> toggleFavoriteLocation(Long locationId, String userEmail) {
        ApiResponse<User> userResponse = getUserByEmail(userEmail);
        if (!userResponse.isSuccess()) return ApiResponse.error("User not found");

        User user = userResponse.getData();
        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) return ApiResponse.error("Location not found");

        boolean isFavorited = user.getFavoriteLocations().stream().anyMatch(l -> l.getId().equals(locationId));
        if (isFavorited) {
            user.getFavoriteLocations().removeIf(l -> l.getId().equals(locationId));
            userRepository.save(user);
            return ApiResponse.success("تم الإزالة من المحفوظات", null);
        } else {
            user.getFavoriteLocations().add(location);
            userRepository.save(user);
            return ApiResponse.success("تم الإضافة إلى المحفوظات بنجاح", null);
        }
    }

    @Override
    public ApiResponse<List<LocationResponse>> getFavoriteLocations(String userEmail) {
        ApiResponse<User> userResponse = getUserByEmail(userEmail);
        if (!userResponse.isSuccess()) return ApiResponse.error("User not found");

        User user = userResponse.getData();
        List<LocationResponse> responses = user.getFavoriteLocations().stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Favorite locations fetched", responses);
    }

    @Override
    @Transactional
    public ApiResponse<LocationResponse> updateCover(Long locationId, UpdateLocationRequest request,
            String currentUserEmail) {
        return updateImage(locationId, request, currentUserEmail, true);
    }

    @Override
    @Transactional
    public ApiResponse<LocationResponse> updateLogo(Long locationId, UpdateLocationRequest request,
            String currentUserEmail) {
        return updateImage(locationId, request, currentUserEmail, false);
    }

    private void mapRequestToEntity(LocationList location, CreateLocationRequest request) {
        location.setName(request.getName());
        location.setDescription(request.getDescription());
        location.setEmail(request.getEmail());
        location.setPhoneNumber(request.getPhoneNumber());
        location.setLogoUrl(request.getLogoUrl());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setCategory(request.getCategory() == null ? LocationCategory.OTHER : request.getCategory());
        location.setFacebookUrl(request.getFacebookUrl());
        location.setInstagramUrl(request.getInstagramUrl());
        location.setLinkedInUrl(request.getLinkedInUrl());
        if (request.getSchedules() != null) {
            location.getSchedules().clear();
            for (com.jomap.backend.DTOs.Locations.LocationSchedule dto : request.getSchedules()) {
                com.jomap.backend.Entities.Locations.LocationSchedule schedule = new com.jomap.backend.Entities.Locations.LocationSchedule();
                schedule.setDayName(dto.getDayName());
                schedule.setStartTime(dto.getStartTime());
                schedule.setEndTime(dto.getEndTime());
                schedule.setIsClosed(dto.getIsClosed() != null ? dto.getIsClosed() : false);
                schedule.setLocation(location);
                location.getSchedules().add(schedule);
            }
        }
        location.setOwnerUpdate(request.getOwnerUpdate());
    }

    private void updateEntityFields(LocationList location, UpdateLocationRequest request) {
        if (request.getName() != null)
            location.setName(request.getName());
        if (request.getDescription() != null)
            location.setDescription(request.getDescription());
        if (request.getLogoUrl() != null)
            location.setLogoUrl(request.getLogoUrl());
        if (request.getCoverUrl() != null)
            location.setCoverUrl(request.getCoverUrl());
        if (request.getEmail() != null)
            location.setEmail(request.getEmail());
        if (request.getPhoneNumber() != null)
            location.setPhoneNumber(request.getPhoneNumber());
        if (request.getLatitude() != null)
            location.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            location.setLongitude(request.getLongitude());

        if (request.getCategory() != null) {
            location.setCategory(LocationCategory.valueOf(request.getCategory().toUpperCase()));
        }

        if (request.getFacebookUrl() != null)
            location.setFacebookUrl(request.getFacebookUrl());
        if (request.getInstagramUrl() != null)
            location.setInstagramUrl(request.getInstagramUrl());
        if (request.getLinkedInUrl() != null)
            location.setLinkedInUrl(request.getLinkedInUrl());
            
        if (request.getSchedules() != null) {
            location.getSchedules().clear();
            for (com.jomap.backend.DTOs.Locations.LocationSchedule dto : request.getSchedules()) {
                com.jomap.backend.Entities.Locations.LocationSchedule schedule = new com.jomap.backend.Entities.Locations.LocationSchedule();
                schedule.setDayName(dto.getDayName());
                schedule.setStartTime(dto.getStartTime());
                schedule.setEndTime(dto.getEndTime());
                schedule.setIsClosed(dto.getIsClosed() != null ? dto.getIsClosed() : false);
                schedule.setLocation(location);
                location.getSchedules().add(schedule);
            }
        }
    }

    private ApiResponse<User> getUserByEmail(String email) {
        if (email == null)
            return ApiResponse.error("Unauthorized");
        return userRepository.findByEmail(email)
                .map(u -> ApiResponse.success("User found", u))
                .orElse(ApiResponse.error("User not found"));
    }

    private ApiResponse<Void> validateCreateRequest(CreateLocationRequest request) {
        if (request.getName() == null || request.getGovernorateId() == null) {
            return ApiResponse.error("البيانات الأساسية مطلوبة");
        }
        return ApiResponse.success("Valid", null);
    }

    private LocationResponse mapToResponse(LocationList location) {
        LocationResponse response = new LocationResponse();
        response.setLocationId(location.getId());
        response.setName(location.getName());
        response.setIsFavorite(false);
        response.setIsBlocked(false);
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                userRepository.findByEmail(email).ifPresent(user -> {
                    boolean isFav = user.getFavoriteLocations().stream().anyMatch(l -> l.getId().equals(location.getId()));
                    response.setIsFavorite(isFav);
                    boolean isBlocked = locationBlockRepository.existsByBlockerAndBlockedLocation(user, location);
                    response.setIsBlocked(isBlocked);
                });
            }
        } catch (Exception ignored) { }
        response.setDescription(location.getDescription());
        response.setEmail(location.getEmail());
        response.setPhoneNumber(location.getPhoneNumber());
        response.setLogoUrl(location.getLogoUrl());
        response.setCoverUrl(location.getCoverUrl());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        if (location.getGovernorate() != null) {
            response.setGovernorateName(location.getGovernorate().getName());
            response.setGovernorateId(location.getGovernorate().getId());
        }
        response.setCategory(location.getCategory());
        response.setStatus(location.getStatus());
        response.setIsActive(location.getActive());
        response.setIsApproved(location.getApproved());
        response.setRating(location.getRating());
        response.setDeletedAt(location.getDeletedAt());
        response.setReviewCount(location.getReviewCount());
        response.setProfileVisits(location.getProfileVisits());
        response.setCreatedAt(location.getCreatedAt());
        if (location.getOwner() != null) {
            response.setOwnerId(location.getOwner().getId());
            response.setOwnerName(location.getOwner().getUsername());
            if (location.getOwner().getProfile() != null) {
                response.setOwnerProfileImageUrl(location.getOwner().getProfile().getProfileImageUrl());
            } else {
                response.setOwnerProfileImageUrl(location.getOwner().getProfileImageUrl());
            }
        }
        response.setFacebookUrl(location.getFacebookUrl());
        response.setInstagramUrl(location.getInstagramUrl());
        response.setLinkedInUrl(location.getLinkedInUrl());
        
        java.util.List<com.jomap.backend.DTOs.Locations.LocationSchedule> scheduleDTOs = new java.util.ArrayList<>();
        boolean isOpenNow = false;
        String currentDayName = getCurrentArabicDayName();
        java.time.LocalTime now = java.time.LocalTime.now();

        if (location.getSchedules() != null) {
            for (com.jomap.backend.Entities.Locations.LocationSchedule s : location.getSchedules()) {
                scheduleDTOs.add(new com.jomap.backend.DTOs.Locations.LocationSchedule(
                    s.getDayName(), s.getStartTime(), s.getEndTime(), s.getIsClosed()
                ));

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
        response.setSchedules(scheduleDTOs);
        response.setIsOpenNow(isOpenNow);
        response.setRejectionReason(location.getRejectionReason());
        
        if (location.getDeletedAt() != null) {
            java.time.LocalDateTime targetTime = location.getDeletedAt().plusHours(24);
            long seconds = java.time.Duration.between(java.time.LocalDateTime.now(), targetTime).getSeconds();
            response.setTimeLeftInSeconds(Math.max(0, seconds));
        } else {
            response.setTimeLeftInSeconds(0L);
        }
        
        return response;
    }

    private ApiResponse<LocationResponse> updateImage(Long locationId, UpdateLocationRequest request, String email,
            boolean isCover) {
        ApiResponse<User> userResponse = getUserByEmail(email);
        if (!userResponse.isSuccess())
            return ApiResponse.error(userResponse.getMessage());

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty())
            return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();

        if (!location.getOwner().getId().equals(userResponse.getData().getId())) {
            return ApiResponse.error("ليس لديك صلاحية لتحديث صور هذا الموقع");
        }

        if (isCover) {
            location.setCoverUrl(request.getCoverUrl());
        } else {
            location.setLogoUrl(request.getLogoUrl());
        }

        locationRepository.save(location);
        return ApiResponse.success("تم تحديث الصورة بنجاح", mapToResponse(location));
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

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<com.jomap.backend.DTOs.Locations.LocationFollowerResponse>> getLocationFollowers(Long locationId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (currentUser == null) {
            return ApiResponse.error("User not found");
        }

        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) {
            return ApiResponse.error("Location not found");
        }

        boolean isOwner = location.getOwner() != null && location.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            return ApiResponse.error("غير مصرح لك بعرض متابعي هذه المنشأة");
        }

        List<com.jomap.backend.Entities.Users.User> followers = userRepository.findFollowersByLocationId(locationId);
        java.util.List<com.jomap.backend.DTOs.Locations.LocationFollowerResponse> result = followers.stream()
            .map(u -> {
                String imgUrl = u.getProfileImageUrl();
                var profile = u.getProfile();
                if (profile != null) {
                    if (profile.getProfileImageUrl() != null) imgUrl = profile.getProfileImageUrl();
                }
                String followedAtStr = java.time.LocalDateTime.now().minusDays(1).toString();
                return new com.jomap.backend.DTOs.Locations.LocationFollowerResponse(
                    u.getId(), u.getUsername(), imgUrl, followedAtStr
                );
            })
            .toList();

        return ApiResponse.success("Followers fetched", result);
    }

    @Override
    @Transactional
    public ApiResponse<String> followLocation(Long locationId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) return ApiResponse.error("Location not found");
        if (location.getOwner() != null && location.getOwner().getId().equals(user.getId())) {
            return ApiResponse.error("لا يمكنك متابعة منشأتك");
        }
        if (user.getFavoriteLocations().stream().anyMatch(l -> l.getId().equals(locationId))) {
            return ApiResponse.success("أنت تتابع هذه المنشأة بالفعل", null);
        }
        user.getFavoriteLocations().add(location);
        userRepository.save(user);
        return ApiResponse.success("تمت متابعة المنشأة", null);
    }

    @Override
    @Transactional
    public ApiResponse<String> unfollowLocation(Long locationId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) return ApiResponse.error("Location not found");
        user.getFavoriteLocations().removeIf(l -> l.getId().equals(locationId));
        userRepository.save(user);
        return ApiResponse.success("تم إلغاء متابعة المنشأة", null);
    }

    @Override
    @Transactional
    public ApiResponse<String> blockLocation(Long locationId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) return ApiResponse.error("Location not found");
        if (location.getOwner() != null && location.getOwner().getId().equals(user.getId())) {
            return ApiResponse.error("لا يمكنك حظر منشأتك");
        }
        if (locationBlockRepository.existsByBlockerAndBlockedLocation(user, location)) {
            return ApiResponse.success("المنشأة محظورة بالفعل", null);
        }
        LocationBlock block = LocationBlock.builder()
                .blocker(user)
                .blockedLocation(location)
                .blockedAt(java.time.LocalDateTime.now())
                .build();
        locationBlockRepository.save(block);
        return ApiResponse.success("تم حظر المنشأة", null);
    }

    @Override
    @Transactional
    public ApiResponse<String> unblockLocation(Long locationId, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail).orElse(null);
        if (user == null) return ApiResponse.error("User not found");
        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) return ApiResponse.error("Location not found");
        locationBlockRepository.deleteByBlockerAndBlockedLocation(user, location);
        return ApiResponse.success("تم إلغاء حظر المنشأة", null);
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ تسجيل زيارة ملف المنشأة
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public ApiResponse<Void> recordVisit(Long locationId, String currentUserEmail) {
        // 1. جلب المنشأة
        LocationList location = locationRepository.findById(locationId).orElse(null);
        if (location == null) {
            return ApiResponse.error("المنشأة غير موجودة");
        }

        // 2. جلب المستخدم الحالي (اختياري — قد يكون ضيفاً)
        User visitor = null;
        if (currentUserEmail != null) {
            visitor = userRepository.findByEmail(currentUserEmail).orElse(null);
        }

        // 3. تجاهل الزيارة إذا كان الزائر هو صاحب المنشأة
        if (visitor != null && location.getOwner() != null
                && location.getOwner().getId().equals(visitor.getId())) {
            return ApiResponse.success("تم تجاهل زيارة الأونر", null);
        }

        // 4. للمستخدمين المسجّلين: منع التكرار خلال ساعة واحدة (cooldown)
        if (visitor != null) {
            LocalDateTime cooldownStart = LocalDateTime.now().minusHours(1);
            boolean alreadyVisited = locationVisitRepository
                    .existsRecentVisitByUser(locationId, visitor.getId(), cooldownStart);
            if (alreadyVisited) {
                return ApiResponse.success("تم احتساب الزيارة مسبقاً", null);
            }
        }

        // 5. تسجيل الزيارة في جدول location_visits
        LocationVisit visit = LocationVisit.builder()
                .location(location)
                .visitor(visitor)
                .visitedAt(LocalDateTime.now())
                .build();
        locationVisitRepository.save(visit);

        // 6. زيادة العداد في المنشأة
        int current = location.getProfileVisits() != null ? location.getProfileVisits() : 0;
        location.setProfileVisits(current + 1);
        locationRepository.save(location);

        return ApiResponse.success("تم تسجيل الزيارة بنجاح", null);
    }
}
