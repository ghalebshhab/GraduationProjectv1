package com.jomap.backend.Entities.Locations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LocationCategory {
    GYM(1, "نادي رياضي"),
    RESTAURANT(2, "مطعم"),
    HOTEL(3, "فندق"),
    CAFE(4, "مقهى"),
    TOURISM(5, "سياحة"),
    MARKET(6, "سوق"),
    MALL(7, "مول"),
    PHARMACY(8, "صيدلية"),
    HOSPITAL(9, "مستشفى"),
    STADIUM(10, "ملعب"),
    PARK(11, "منتزه"),
    GAS_STATION(12, "محطة وقود"),
    EDUCATION(13, "تعليم"),
    ENTERTAINMENT(14, "ترفيه"),
    VOLUNTEER_TEAM(15, "فريق تطوعي"), 
    ORGANIZATION(16, "منظمة/جمعية"), 
    OTHER(17, "أخرى");

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