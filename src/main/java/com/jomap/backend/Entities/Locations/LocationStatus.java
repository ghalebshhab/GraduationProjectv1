package com.jomap.backend.Entities.Locations;

public enum LocationStatus {
    PENDING(1, "بانتظار الموافقة"), // قيد الانتظار - بانتظار موافقة الأدمن
    APPROVED(2, "مقبول"), // مقبول - الأدمن وافق ولكن لم ينشر بعد
    REJECTED(3, "مرفوض"), // مرفوض - الأدمن رفض الطلب ويحتاج تعديل
    PUBLISHED(4, "منشور"), // منشور - ظاهر للجميع على الخريطة
    DEACTIVATED(5, "معطل"), // معطل - المالك أوقفه مؤقتاً
    DELETED(6, "محذوف"); // محذوف - أزيل من النظام 

    private final int id;
    private final String label;

    LocationStatus(int id, String label) {
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