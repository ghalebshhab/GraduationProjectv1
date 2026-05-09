package com.jomap.backend.Entities.Activities;

public enum ActivityStatus {
    PENDING(1, "بانتظار الموافقة"),
    APPROVED(2, "مقبولة"),
    REJECTED(3, "مرفوضة"),
    CANCELLED(4, "ملغاة"),
    POSTPONED(5, "مؤجلة"),
    COMPLETED(6, "منتهية");

    private final int id;
    private final String label;

    ActivityStatus(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}