package com.hospital.management.Controller;

import com.hospital.management.Entity.City;
import com.hospital.management.Service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CityController {

    private final CityService cityService;

    /**
     * Tüm şehirleri alfabetik olarak getirir.
     * GET http://localhost:8080/api/cities
     */
    @GetMapping
    public ResponseEntity<List<City>> getAllCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    /**
     * ID'ye göre şehir detayını getirir.
     * GET http://localhost:8080/api/cities/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<City> getCityById(@PathVariable Long id) {
        return ResponseEntity.ok(cityService.getById(id));
    }
}