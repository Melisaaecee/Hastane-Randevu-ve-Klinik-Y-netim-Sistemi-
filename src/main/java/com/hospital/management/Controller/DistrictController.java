package com.hospital.management.Controller;

import com.hospital.management.Entity.District;
import com.hospital.management.Service.DistrictService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
public class DistrictController {

    private final DistrictService districtService;

    // ŞEHRE GÖRE İLÇELER (Dropdown için kritik)
    @GetMapping("/city/{cityId}")
    public List<District> getDistrictsByCity(@PathVariable Long cityId) {
        return districtService.getDistrictsByCity(cityId);
    }

    //  ID İLE GETİR
    @GetMapping("/{id}")
    public District getById(@PathVariable Long id) {
        return districtService.getById(id);
    }

    //  VALIDATION ENDPOINT (Opsiyonel ama güçlü)
    @GetMapping("/validate")
    public ResponseEntity<String> validateDistrictInCity(@RequestParam Long districtId,
            @RequestParam Long cityId) {

        districtService.validateDistrictInCity(districtId, cityId);
        return ResponseEntity.ok("Valid district-city match");
    }
}