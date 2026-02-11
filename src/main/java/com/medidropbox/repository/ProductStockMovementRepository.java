package com.medidropbox.repository;

import com.medidropbox.entity.ProductStockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStockMovementRepository extends JpaRepository<ProductStockMovement, Long> {

    List<ProductStockMovement> findByProductIdOrderByCreatedAtDesc(Long productId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COALESCE(SUM(m.quantity), 0) FROM ProductStockMovement m WHERE m.product.id = :productId")
    int sumQuantityByProductId(@Param("productId") Long productId);
}
