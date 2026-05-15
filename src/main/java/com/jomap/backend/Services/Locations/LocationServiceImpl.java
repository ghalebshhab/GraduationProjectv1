package com.jomap.backend.Services.Locations;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Locations.CreateLocationRequest;
import com.jomap.backend.DTOs.Locations.LocationResponse;
import com.jomap.backend.DTOs.Locations.UpdateLocationRequest;
import com.jomap.backend.Entities.Gove.Governorate;
import com.jomap.backend.Entities.Gove.GovernorateRepository;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Locations.LocationCategory;
import com.jomap.backend.Entities.Locations.LocationStatus;
import com.jomap.backend.Entities.Users.Role;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;
import com.jomap.backend.Services.Auth.JwtService;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LocationServiceImpl implements LocationService {
    
    private final LocationRepo locationRepository; 
    private final UserRepository userRepository;
    private final GovernorateRepository governorateRepository;
    private final JwtService jwtService;

    @Override
public ApiResponse<LocationResponse> createLocation(CreateLocationRequest request, String currentUserEmail) {
    ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
    if (!userResponse.isSuccess()) return ApiResponse.error(userResponse.getMessage());

    User owner = userResponse.getData();
    
    if (request.getGovernorateId() == null) {
        return ApiResponse.error("يجب اختيار المحافظة");
    }

    ApiResponse<Void> validationResponse = validateCreateRequest(request);
    if (!validationResponse.isSuccess()) return ApiResponse.error(validationResponse.getMessage());

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

                 // 1. حفظ الموقع في الداتابيز
                LocationList savedLocation = locationRepository.save(location);

                // 2. ترقية المستخدم ليكون OWNER وحفظه
                owner.setRole(Role.OWNER); 
                userRepository.save(owner);

                // 3. توليد توكن جديد يحتوي على الرتبة الجديدة (OWNER)
                // تأكد أن دالة generateToken في JwtService تأخذ الإيميل وتجلب الرتبة من قاعدة البيانات
                String newToken = jwtService.generateToken(owner.getEmail());

                // 4. تجهيز الرد وإضافة التوكن فيه
                LocationResponse response = mapToResponse(savedLocation);
                response.setNewToken(newToken); // تأكد من إضافة هذا الحقل في كلاس LocationResponse

                return ApiResponse.success(
                        "تم إنشاء الموقع بنجاح، بانتظار موافقة المسؤول.",
                        mapToResponse(savedLocation)
                );
            }).orElse(ApiResponse.error("المحافظة المحددة غير موجودة في النظام"));
}


    @Override
    public ApiResponse<LocationResponse> updateLocation(Long locationId, UpdateLocationRequest request, String currentUserEmail) {
        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
        if (!userResponse.isSuccess()) return ApiResponse.error(userResponse.getMessage());

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();

        // التأكد أن المعدل هو المالك
        if (!location.getOwner().getId().equals(userResponse.getData().getId())) {
            return ApiResponse.error("ليس لديك صلاحية لتعديل هذا الموقع");
        }

        // تحديث الحقول
        updateEntityFields(location, request);

        // حسب المخطط: التعديل يعيد الموقع لحالة الانتظار للمراجعة
        location.setStatus(LocationStatus.PENDING);
        location.setApproved(false); 

        return ApiResponse.success("تم تحديث البيانات، بانتظار المراجعة.", mapToResponse(locationRepository.save(location)));
    }

    @Override
    public ApiResponse<LocationResponse> approveLocation(Long locationId) {
        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();
        
        // الانتقال للحالات النهائية للنشر
        location.setStatus(LocationStatus.PUBLISHED);
        location.setApproved(true);
        location.setActive(true);

        return ApiResponse.success("تمت الموافقة ونشر الموقع بنجاح.", mapToResponse(locationRepository.save(location)));
    }

    @Override
    public ApiResponse<LocationResponse> deactivateLocation(Long locationId, String currentUserEmail) {
        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
        if (!userResponse.isSuccess()) return ApiResponse.error(userResponse.getMessage());

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();
        
        // تعطيل الموقع مؤقتاً
        location.setStatus(LocationStatus.DEACTIVATED);
        location.setApproved(false); // يسحب من العرض العام

        return ApiResponse.success("تم إيقاف نشاط الموقع مؤقتاً.", mapToResponse(locationRepository.save(location)));
    }

    @Override
    public ApiResponse<LocationResponse> getLocationById(Long locationId) {
        return locationRepository.findById(locationId)
                .map(loc -> ApiResponse.success("Fetched", mapToResponse(loc)))
                .orElse(ApiResponse.error("Not found"));
    }

    @Override
    public ApiResponse<List<LocationResponse>> getLocations(Long governorateId, LocationCategory category) {
        // البحث فقط عن المواقع التي حالتها PUBLISHED لضمان ظهور المعتمد فقط
        List<LocationList> locations = locationRepository.findByActiveTrueAndApprovedTrue();
        return ApiResponse.success("Success", locations.stream().map(this::mapToResponse).toList());
    }

    @Override
    public ApiResponse<LocationResponse> getMyLocation(String currentUserEmail) {
        ApiResponse<User> userResponse = getUserByEmail(currentUserEmail);
        if (!userResponse.isSuccess()) return ApiResponse.error(userResponse.getMessage());

        return locationRepository.findByOwnerId(userResponse.getData().getId())
                .map(loc -> ApiResponse.success("My location", mapToResponse(loc)))
                .orElse(ApiResponse.error("No location found"));
    }

// --- تحديث صورة الغلاف (Cover) ---
    @Override
    public ApiResponse<LocationResponse> updateCover(Long locationId, String coverUrl, String currentUserEmail) {
        return updateImage(locationId, coverUrl, currentUserEmail, true);
    }

    // --- تحديث اللوجو (Logo) ---
    @Override
    public ApiResponse<LocationResponse> updateLogo(Long locationId, String logoUrl, String currentUserEmail) {
        return updateImage(locationId, logoUrl, currentUserEmail, false);
    }


    // --- Helper Methods ---

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
        location.setWorkingHours(request.getWorkingHours());
        location.setOwnerUpdate(request.getOwnerUpdate());
    }

    private void updateEntityFields(LocationList location, UpdateLocationRequest request) {
        if (request.getName() != null) location.setName(request.getName());
        if (request.getDescription() != null) location.setDescription(request.getDescription());
        if (request.getLogoUrl() != null) location.setLogoUrl(request.getLogoUrl());
        if (request.getCategory() != null) location.setCategory(request.getCategory());
        if (request.getWorkingHours() != null) location.setWorkingHours(request.getWorkingHours());
        // ... إضافة باقي الحقول حسب الحاجة
    }

    private ApiResponse<User> getUserByEmail(String email) {
        if (email == null) return ApiResponse.error("Unauthorized");
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
        response.setRating(location.getRating());
        response.setReviewCount(location.getReviewCount());
        response.setCreatedAt(location.getCreatedAt());
        if (location.getOwner() != null) {
            response.setOwnerId(location.getOwner().getId());
            response.setOwnerName(location.getOwner().getUsername());
        }
        return response;
    }

    // دالة مساعدة لتجنب تكرار الكود
    private ApiResponse<LocationResponse> updateImage(Long locationId, String imageUrl, String email, boolean isCover) {
        ApiResponse<User> userResponse = getUserByEmail(email);
        if (!userResponse.isSuccess()) return ApiResponse.error(userResponse.getMessage());

        Optional<LocationList> locationOptional = locationRepository.findById(locationId);
        if (locationOptional.isEmpty()) return ApiResponse.error("الموقع غير موجود");

        LocationList location = locationOptional.get();

        // التأكد أن الشخص هو المالك الحقيقي
        if (!location.getOwner().getId().equals(userResponse.getData().getId())) {
            return ApiResponse.error("ليس لديك صلاحية لتحديث صور هذا الموقع");
        }

        // تحديث الحقل المطلوب
        if (isCover) {
            location.setCoverUrl(imageUrl); // تأكد أن حقل coverUrl موجود في LocationList
        } else {
            location.setLogoUrl(imageUrl);
        }

        locationRepository.save(location);
        return ApiResponse.success("تم تحديث الصورة بنجاح", mapToResponse(location));
    }
}