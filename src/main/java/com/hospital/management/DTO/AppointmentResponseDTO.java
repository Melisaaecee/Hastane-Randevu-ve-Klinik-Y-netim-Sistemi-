package com.hospital.management.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class AppointmentResponseDTO {
    private Long id;
    private String doctorName;
    private String clinicName;
    private String appointmentDate;
    private String status;
    private boolean canCancel; // Randevu gelecekteyse iptal butonu görünsün mü?
    private String patientName; // Hasta Ad Soyad
    private String patientTckn; // Hasta TCKN
    private String hospitalName; // Hastane Adı
    private String cityName; // Şehir Adı
    private String districtName; // İlçe Adı


    public AppointmentResponseDTO() {
    }
}