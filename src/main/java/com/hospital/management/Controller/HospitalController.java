package com.hospital.management.Controller;

import com.hospital.management.Entity.Hospital;
import com.hospital.management.Service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HospitalController {

    private final HospitalService hospitalService;

   
    //  LİSTELEME VE FİLTRELEME (RANDAVU AKIŞI)
   

    /**
     * Tüm hastaneleri alfabetik listeler.
     * GET http://localhost:8080/api/hospitals
     */
    @GetMapping
    public ResponseEntity<List<Hospital>> getAllHospitals() {
        return ResponseEntity.ok(hospitalService.getAllHospitals());
    }

    /**
     * Belirli bir ilçeye ait hastaneleri getirir.
     * GET http://localhost:8080/api/hospitals/district/{districtId}
     */
    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<Hospital>> getHospitalsByDistrict(@PathVariable Long districtId) {
        return ResponseEntity.ok(hospitalService.getHospitalsByDistrict(districtId));
    }

    /**
     * Belirli bir şehre ait TÜM hastaneleri getirir.
     * GET http://localhost:8080/api/hospitals/city/{cityId}
     */
    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Hospital>> getHospitalsByCity(@PathVariable Long cityId) {
        return ResponseEntity.ok(hospitalService.getHospitalsByCity(cityId));
    }

    /**
     * İlçe içinde isme göre hastane arar.
     * GET http://localhost:8080/api/hospitals/search?districtId=1&name=Devlet
     */
    @GetMapping("/search")
    public ResponseEntity<List<Hospital>> searchHospitals(
            @RequestParam Long districtId, 
            @RequestParam String name) {
        return ResponseEntity.ok(hospitalService.searchHospitalsInDistrict(districtId, name));
    }

    
    //  YÖNETİMSEL İŞLEMLER (ADMIN)
   

    /**
     * Yeni hastane ekler veya günceller.
     * POST http://localhost:8080/api/hospitals
     */
    @PostMapping
    public ResponseEntity<Hospital> saveOrUpdate(@RequestBody Hospital hospital) {
        return ResponseEntity.ok(hospitalService.saveOrUpdate(hospital));
    }

    /**
     * Hastaneyi siler.
     * DELETE http://localhost:8080/api/hospitals/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.ok("Hastane başarıyla silindi.");
    }
}