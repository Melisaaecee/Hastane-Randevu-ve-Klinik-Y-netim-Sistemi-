package com.hospital.management.Repository;

import com.hospital.management.Entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    
    // Şehir ismine göre arama yapmak istersen (Örn: Veri girişi kontrolü için)
    Optional<City> findByName(String name);
    
    // Şehir isminin var olup olmadığını kontrol etmek için
    boolean existsByName(String name);
}
