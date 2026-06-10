package com.jomap.backend.Entities.Activities;

public enum RegistrationStatus {
    PENDING(1, "بانتظار الموافقة"),
    APPROVED(2, "مقبول"),
    REJECTED(3, "مرفوض"),
    CANCELLED(4, "ملغى"),
    WITHDRAWN(5, "منسحب");

    private final int id;
    private final String label;

    RegistrationStatus(int id, String label) {
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
