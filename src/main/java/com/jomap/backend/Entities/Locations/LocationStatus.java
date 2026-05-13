package com.jomap.backend.Entities.Locations;

public enum LocationStatus {
    PENDING_APPROVAL,  // قيد الانتظار - بانتظار موافقة الأدمن
    REJECTED,          // مرفوض - الأدمن رفض الطلب ويحتاج تعديل
    APPROVED,          // مقبول - الأدمن وافق ولكن لم ينشر بعد
    PUBLISHED,         // منشور - ظاهر للجميع على الخريطة
    DEACTIVATED,       // معطل - المالك أوقفه مؤقتاً
    DELETED            // محذوف - أزيل من النظام
}