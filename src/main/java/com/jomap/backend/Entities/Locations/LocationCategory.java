package com.jomap.backend.Entities.Locations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LocationCategory {
    GYM(1, "نادي رياضي"),
    RESTAURANT(2, "مطعم"),
    HOTEL(3, "فندق"),
    CAFE(4, "مقهى"),
    TOURISM(5, "سياحة"),
    HISTORICAL(6, "أماكن أثرية"),
    MARKET(7, "سوق"),
    MALL(8, "مول"),
    PHARMACY(9, "صيدلية"),
    HOSPITAL(10, "مستشفى"),
    STADIUM(11, "ملعب"),
    PARK(12, "منتزه"),
    GAS_STATION(13, "محطة وقود"),
    EDUCATION(14, "تعليم"),
    ENTERTAINMENT(15, "ترفيه"),
    VOLUNTEER_TEAM(16, "فريق تطوعي"), 
    ORGANIZATION(17, "منظمة/جمعية"), 
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