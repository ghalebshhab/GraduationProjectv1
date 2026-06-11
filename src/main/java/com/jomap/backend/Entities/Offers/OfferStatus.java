package com.jomap.backend.Entities.Offers;

public enum OfferStatus {
    ACTIVE(1, "نشط"),
    CANCELLED(2, "ملغى"),
    EXPIRED(3, "منتهي الصلاحية"),
    SCHEDULED(4, "مجدول");

    private final int id;
    private final String label;

    OfferStatus(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
}