package com.medidropbox.repository;

import com.medidropbox.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByHospitalIdAndIsActiveTrueOrderByName(Long hospitalId);

    List<Product> findByHospitalIdOrderByName(Long hospitalId);

    Optional<Product> findByIdAndHospitalId(Long id, Long hospitalId);

    Optional<Product> findByHospitalIdAndSkuAndIdNot(Long hospitalId, String sku, Long excludeId);

    Optional<Product> findByHospitalIdAndSku(Long hospitalId, String sku);

    boolean existsByHospitalIdAndSku(Long hospitalId, String sku);
}
