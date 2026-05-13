package com.jomap.backend.Entities.Locations;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum LocationCategory {
    GYM("نادي رياضي"),
    RESTAURANT("مطعم"),
    HOTEL("فندق"),
    CAFE("مقهى"),
    TOURISM("سياحة"),
    MARKET("سوق"),
    MALL("مول"),
    PHARMACY("صيدلية"),
    HOSPITAL("مستشفى"),
    STADIUM("ملعب"),
    PARK("منتزه"),
    GAS_STATION("محطة وقود"),
    EDUCATION("تعليم"),
    ENTERTAINMENT("ترفيه"),
    VOLUNTEER_TEAM("فريق تطوعي"), 
    OTHER("أخرى");

    private final String displayName;

    LocationCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}