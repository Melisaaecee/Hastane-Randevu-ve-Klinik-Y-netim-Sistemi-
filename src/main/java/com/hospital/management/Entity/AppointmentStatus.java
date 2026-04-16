package com.hospital.management.Entity;

public enum AppointmentStatus {
    APPROVED, // Onaylandı
    COMPLETED, // Tamamlandı -- geçmiş randevular 
    NOT_ATTENDED, // Gelmedi
    CANCELED // İptal Edildi
}
