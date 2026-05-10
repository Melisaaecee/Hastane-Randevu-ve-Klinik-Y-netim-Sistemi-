package com.hospital.management.Repository;

import com.hospital.management.Entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

    // Şehir ismine göre arama yapmak istersen (Örn: Veri girişi kontrolü için)
    Optional<City> findByName(String name);

    // Şehir isminin var olup olmadığını kontrol etmek için
    boolean existsByName(String name);

    // Şehirleri her zaman alfabetik getirmek için bir metod ekleyebiliriz
    List<City> findAllByOrderByNameAsc();

    @Query("SELECT DISTINCT c FROM City c LEFT JOIN FETCH c.districts")
    List<City> findAllWithDistricts();
}
