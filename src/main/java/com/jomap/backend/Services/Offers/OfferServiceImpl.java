package com.jomap.backend.Services.Offers;

import com.jomap.backend.DTOs.ApiResponse;
import com.jomap.backend.DTOs.Offers.OfferProductResponse;
import com.jomap.backend.DTOs.Offers.OfferRequest;
import com.jomap.backend.DTOs.Offers.OfferResponse;
import com.jomap.backend.Entities.Governorate.Governorate;
import com.jomap.backend.Entities.Governorate.GovernorateRepository;
import com.jomap.backend.Entities.Locations.LocationList;
import com.jomap.backend.Entities.Locations.LocationRepo;
import com.jomap.backend.Entities.Offers.Offer;
import com.jomap.backend.Entities.Offers.OfferProduct;
import com.jomap.backend.Entities.Offers.OfferRepo;
import com.jomap.backend.Entities.Offers.OfferStatus;
import com.jomap.backend.Entities.Posts.Post;
import com.jomap.backend.Entities.Posts.PostRepository;
import com.jomap.backend.Entities.Users.User;
import com.jomap.backend.Entities.Users.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepo offerRepo;
    private final LocationRepo locationRepo;
    private final GovernorateRepository governorateRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public OfferServiceImpl(OfferRepo offerRepo, 
                            LocationRepo locationRepo, 
                            GovernorateRepository governorateRepository,
                            UserRepository userRepository,
                            PostRepository postRepository) {
        this.offerRepo = offerRepo;
        this.locationRepo = locationRepo;
        this.governorateRepository = governorateRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    @Transactional
    public ApiResponse<OfferResponse> createOffer(OfferRequest request, String email) {
        
        try {
            java.time.LocalDate startDate = java.time.LocalDate.parse(request.getStartDate());
            java.time.LocalDate endDate = java.time.LocalDate.parse(request.getEndDate());
            java.time.LocalTime startTime = parseTime(request.getStartTime());
            java.time.LocalTime endTime = parseTime(request.getEndTime());

            if (endDate.isBefore(startDate)) {
                return ApiResponse.error("لا يمكن أن يكون تاريخ النهاية أقدم من تاريخ البداية");
            }
            if (startDate.equals(endDate) && !endTime.isAfter(startTime)) {
                return ApiResponse.error("في نفس اليوم، يجب أن يكون وقت النهاية بعد وقت البداية");
            }
        } catch (Exception e) {
            return ApiResponse.error("صيغة التاريخ أو الوقت غير صحيحة، الرجاء التأكد من الإدخال");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("فشل الإنشاء: المستخدم غير موجود في النظام");
        }
        User user = userOptional.get();

            Optional<LocationList> locationOptional = locationRepo.findById(request.getLocationId());
            if (locationOptional.isEmpty()) {
                return ApiResponse.error("فشل الإنشاء: المنشأة المحددة غير موجودة");
            }
            LocationList location = locationOptional.get();

            if (location.getStatus() != com.jomap.backend.Entities.Locations.LocationStatus.PUBLISHED) {
                return ApiResponse.error("عذراً، يجب أن تكون حالة المنشأة منشورة (PUBLISHED) لتتمكن من إضافة عرض");
            }

            if (location.getCategory() == com.jomap.backend.Entities.Locations.LocationCategory.VOLUNTEER_TEAM || 
                location.getCategory() == com.jomap.backend.Entities.Locations.LocationCategory.ORGANIZATION) {
                return ApiResponse.error("عذراً، الأفرقة التطوعية والمنظمات غير مصرح لها بإضافة عروض");
            }

            Optional<Governorate> optionalGov = governorateRepository.findById(request.getGovernorateId());
            if (optionalGov.isEmpty()) {
                return ApiResponse.error("العملية مرفوضة: المحافظة المحددة غير مدعومة حالياً");
            }

            Offer offer = new Offer();
            offer.setImageUrl(request.getImageUrl());
            offer.setTitle(request.getTitle());
            offer.setDescription(request.getDescription());
            offer.setScheduleType(request.getScheduleType());
            offer.setStartDate(request.getStartDate());
            offer.setEndDate(request.getEndDate());
            offer.setStartTime(request.getStartTime());
            offer.setEndTime(request.getEndTime());
            offer.setLatitude(request.getLatitude());
            offer.setLongitude(request.getLongitude());
            offer.setLocation(location);
            offer.setGovernorate(optionalGov.get());
            offer.setStatus(OfferStatus.ACTIVE);
            offer.setCreatedBy(user);
            offer.setClicksCount(request.getClicksCount() != null ? request.getClicksCount() : 0);
            offer.setRenewedFromOfferId(request.getRenewedFromOfferId());

            List<OfferProduct> products = new ArrayList<>();
            if (request.getProducts() != null && !request.getProducts().isEmpty()) {
                for (com.jomap.backend.DTOs.Offers.OfferProductRequest prodReq : request.getProducts()) {
                    OfferProduct product = new OfferProduct();
                    product.setProductName(prodReq.getProductName());
                    product.setPriceBefore(prodReq.getPriceBefore());
                    product.setPriceAfter(prodReq.getPriceAfter());
                    product.setOffer(offer);
                    products.add(product);
                }
            }
            offer.setProducts(products);

            Offer savedOffer = offerRepo.save(offer);

            // تحويل العرض إلى بوست تلقائي بـ محتوى واضح ونظيف
            String postContent = "New Offer: " + (savedOffer.getTitle() != null ? savedOffer.getTitle() : "") 
                               + "\n" + (savedOffer.getDescription() != null ? savedOffer.getDescription() : "");
            if (postContent.length() > 2000) {
                postContent = postContent.substring(0, 1997) + "...";
            }

            Post offerPost = new Post(
                    user,
                    postContent,
                    savedOffer.getImageUrl(),
                    Post.PostType.COMMUNITY
            );
            offerPost.setLatitude(savedOffer.getLatitude());
            offerPost.setLongitude(savedOffer.getLongitude());
            offerPost.setCategory("OFFER");
            
            // ✨ التعديل الهندسي الجديد بناءً على طلبك:
            offerPost.setOfferId(savedOffer.getId());    // تخزين المعرف بالحقل الجديد الخاص بالعروض 🚀
            offerPost.setActivityId(null);               // تصفير حقل الأنشطة تماماً هنا

            postRepository.save(offerPost);

            return ApiResponse.success("تم إضافة العرض بنجاح", mapToResponse(savedOffer));

    }

    @Override
    public ApiResponse<List<OfferResponse>> getMyOffers(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("لا يمكن جلب البيانات: المستخدم غير موثق");
        }

        List<OfferResponse> offers = offerRepo.findByCreatedById(userOptional.get().getId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.success("تم جلب عروضك بنجاح", offers);
    }

    @Override
    public ApiResponse<OfferResponse> getOfferById(Long offerId) {
        return offerRepo.findById(offerId)
                .map(offer -> ApiResponse.success("تم جلب تفاصيل العرض بنجاح", mapToResponse(offer)))
                .orElse(ApiResponse.error("العرض غير موجود"));
    }

    private OfferResponse mapToResponse(Offer offer) {
        List<OfferProductResponse> productDtos = new ArrayList<>();
        if (offer.getProducts() != null) {
            for (OfferProduct p : offer.getProducts()) {
                productDtos.add(OfferProductResponse.builder()
                        .id(p.getId())
                        .productName(p.getProductName())
                        .priceBefore(p.getPriceBefore())
                        .priceAfter(p.getPriceAfter())
                        .build());
            }
        }

        String phone = (offer.getLocation() != null && offer.getLocation().getPhoneNumber() != null) 
                        ? offer.getLocation().getPhoneNumber() 
                        : (offer.getCreatedBy() != null ? offer.getCreatedBy().getPhoneNumber() : null);

        boolean isFavorite = false;
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isPresent()) {
                    isFavorite = userOpt.get().getFavoriteOffers().stream().anyMatch(o -> o.getId().equals(offer.getId()));
                }
            }
        } catch (Exception ignored) { }

        return OfferResponse.builder()
                .id(offer.getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .scheduleType(offer.getScheduleType())
                .startDate(offer.getStartDate())
                .endDate(offer.getEndDate())
                .startTime(offer.getStartTime())
                .endTime(offer.getEndTime())
                .products(productDtos)
                .locationId(offer.getLocation().getId())
                .locationName(offer.getLocation().getName())
                .imageUrl(offer.getImageUrl())
                .latitude(offer.getLatitude())
                .longitude(offer.getLongitude())
                .governorateId(offer.getGovernorate().getId())
                .governorateName(offer.getGovernorate().getName())
                .statusId((long) offer.getStatus().getId())
                .createdById(offer.getCreatedBy().getId())
                .createdByUsername(offer.getLocation() != null ? offer.getLocation().getName() : offer.getCreatedBy().getUsername())
                .phoneNumber(phone)
                .locationPhone(phone)
                .viewsCount(offer.getViewsCount() != null ? offer.getViewsCount() : 0)
                .clicksCount(offer.getClicksCount() != null ? offer.getClicksCount() : 0)
                .cancelledAt(offer.getCancelledAt())
                .isRenewed(offer.getRenewedFromOfferId() != null)
                .isFavorite(isFavorite)
                .build();
    }
    
    @Override
    public ApiResponse<List<OfferResponse>> getOffersByLocation(Long locationId) {
        List<OfferResponse> offers = offerRepo.findByLocationId(locationId)
                .stream()
                .filter(o -> o.getStatus() == OfferStatus.ACTIVE || o.getStatus() == OfferStatus.EXPIRED)
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("تم جلب العروض بنجاح", offers);
    }

    @Override
    @Transactional
    public ApiResponse<String> toggleFavoriteOffer(Long offerId, String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) return ApiResponse.error("User not found");

        User user = userOptional.get();
        Offer offer = offerRepo.findById(offerId).orElse(null);
        if (offer == null) return ApiResponse.error("Offer not found");

        boolean isFavorited = user.getFavoriteOffers().stream().anyMatch(o -> o.getId().equals(offerId));
        if (isFavorited) {
            user.getFavoriteOffers().removeIf(o -> o.getId().equals(offerId));
            userRepository.save(user);
            return ApiResponse.success("تم الإزالة من المحفوظات", null);
        } else {
            user.getFavoriteOffers().add(offer);
            userRepository.save(user);
            return ApiResponse.success("تم الإضافة إلى المحفوظات بنجاح", null);
        }
    }

    @Override
    public ApiResponse<List<OfferResponse>> getFavoriteOffers(String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) return ApiResponse.error("User not found");

        User user = userOptional.get();
        List<OfferResponse> responses = user.getFavoriteOffers().stream()
                .map(this::mapToResponse)
                .toList();
        return ApiResponse.success("Favorite offers fetched", responses);
    }

    @Override
    public ApiResponse<com.jomap.backend.DTOs.PaginatedResponse<OfferResponse>> getAllOffers(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Offer> offerPage = offerRepo.findAllByOrderByIdDesc(pageable);
        
        org.springframework.data.domain.Page<OfferResponse> responsePage = offerPage.map(this::mapToResponse);
        return ApiResponse.success("تم جلب العروض بنجاح", com.jomap.backend.DTOs.PaginatedResponse.from(responsePage));
    }

    @Override
    @Transactional
    public ApiResponse<OfferResponse> cancelOffer(Long offerId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }
        User user = userOptional.get();

        Optional<Offer> offerOptional = offerRepo.findById(offerId);
        if (offerOptional.isEmpty()) {
            return ApiResponse.error("العرض غير موجود");
        }

        Offer offer = offerOptional.get();
        
        // التحقق من أن المستخدم هو من أنشأ العرض
        if (!offer.getCreatedBy().getId().equals(user.getId())) {
            return ApiResponse.error("غير مصرح لك بإلغاء هذا العرض");
        }

        if (offer.getStatus() == OfferStatus.CANCELLED) {
            return ApiResponse.error("العرض ملغى مسبقاً");
        }

        offer.setStatus(OfferStatus.CANCELLED);
        
        // Format the date properly for UI (e.g. "yyyy-MM-dd HH:mm:ss")
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        offer.setCancelledAt(java.time.LocalDateTime.now().format(formatter));

        Offer savedOffer = offerRepo.save(offer);
        
        return ApiResponse.success("تم إلغاء العرض بنجاح", mapToResponse(savedOffer));
    }

    @Override
    @Transactional
    public ApiResponse<OfferResponse> deleteOffer(Long offerId, String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ApiResponse.error("المستخدم غير موجود");
        }
        User user = userOptional.get();

        Optional<Offer> offerOptional = offerRepo.findById(offerId);
        if (offerOptional.isEmpty()) {
            return ApiResponse.error("العرض غير موجود");
        }

        Offer offer = offerOptional.get();
        
        // التحقق من أن المستخدم هو من أنشأ العرض
        if (!offer.getCreatedBy().getId().equals(user.getId())) {
            return ApiResponse.error("غير مصرح لك بحذف هذا العرض");
        }

        if (offer.getStatus() == OfferStatus.DELETED) {
            return ApiResponse.error("العرض محذوف مسبقاً");
        }

        offer.setStatus(OfferStatus.DELETED);

        Offer savedOffer = offerRepo.save(offer);
        
        return ApiResponse.success("تم حذف العرض بنجاح", mapToResponse(savedOffer));
    }

    private java.time.LocalTime parseTime(String time) {
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
            return java.time.LocalTime.parse(time.trim(), formatter);
        } catch (Exception e) {
            return java.time.LocalTime.parse(time.trim()); // standard HH:mm or HH:mm:ss
        }
    }
}