package com.jomap.backend.Entities.Offers;

public enum OfferStatus {
    PENDING(1, "بانتظار الموافقة"),
    APPROVED(2, "مقبولة"),
    REJECTED(3, "مرفوضة"),
    CANCELLED(4, "ملغاة"),
    COMPLETED(5, "منتهية");

    private final int id;
    private final String label;

    OfferStatus(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() { return id; }
    public String getLabel() { return label; }
}