package com.jomap.backend.Entities.Locations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LocationCategory {
    FOOD_AND_BEVERAGE(1, "مطاعم ومقاهي"),
    RETAIL_AND_SHOPPING(2, "تسوق ومتاجر"),
    SUPERMARKETS_AND_BAKERIES(3, "سوبرماركت ومخابز"),
    HOTELS_AND_RESORTS(4, "فنادق ومنتجعات"),
    TOURISM_AND_HISTORICAL(5, "سياحة وأماكن أثرية"),
    MEDICAL_AND_HEALTH(6, "مراكز طبية وعيادات"),
    PHARMACIES_AND_COSMETICS(7, "صيدليات وعناية بالبشرة"),
    SPORTS_AND_FITNESS(8, "رياضة ولياقة بدنية"),
    ENTERTAINMENT_AND_LEISURE(9, "ترفيه ومتنزهات"),
    EDUCATION_AND_TRAINING(10, "تعليم وتدريب"),
    BEAUTY_AND_SALONS(11, "صالونات وتجميل"),
    CARS_AND_MAINTENANCE(12, "سيارات وخدماتها"),
    EVENTS_AND_HALLS(13, "قاعات ومناسبات"),
    REAL_ESTATE_AND_CONTRACTING(14, "عقارات ومقاولات"),
    PUBLIC_SERVICES(15, "خدمات عامة"),
    VOLUNTEER_TEAM(16, "فريق تطوعي"), 
    ORGANIZATION(17, "منظمة/جمعية"), 
    RESTAURANT(19, "مطعم"),
    CAFE(20, "مقهى"),
    OTHER(18, "أخرى");

    private final int id;
    private final String label;

    LocationCategory(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    @JsonValue 
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static LocationCategory fromString(String value) {
        for (LocationCategory category : LocationCategory.values()) {
            if (category.name().equalsIgnoreCase(value) || String.valueOf(category.id).equals(value)) {
                return category;
            }
        }
        return OTHER;
    }
}