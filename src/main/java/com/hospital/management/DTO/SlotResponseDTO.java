package com.hospital.management.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SlotResponseDTO {
    private Long id;
    private String doctorName;     // "Dr. Ad Soyad" şeklinde hazır veri
    private String startTime;      // "15.05.2026 10:30" formatında
    private String endTime;
    private String status;         // AVAILABLE, BOOKED vb.
    private boolean isFull;        // Frontend'deki badge rengi için
}
