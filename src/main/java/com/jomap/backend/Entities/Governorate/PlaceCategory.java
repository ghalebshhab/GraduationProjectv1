package com.jomap.backend.Entities.Governorate;

import lombok.Getter;

@Getter
public enum PlaceCategory {
    HISTORICAL(1, "موقع أثري/تاريخي"),
    NATURE(2, "طبيعية ومحميات"),
    MUSEUM(3, "متحف"),
    RELIGIOUS(4, "موقع ديني"),
    ENTERTAINMENT(5, "ترفيه وسياحة");

    private final int id;
    private final String label;

    PlaceCategory(int id, String label) {
        this.id = id;
        this.label = label;
    }
}