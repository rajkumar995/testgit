package com.medidropbox.repository;

import com.medidropbox.entity.PharmacyCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyCounterRepository extends JpaRepository<PharmacyCounter, Long> {
    
    /**
     * Find open counter for a hospital (should be only one)
     */
    Optional<PharmacyCounter> findByHospitalIdAndIsOpenTrue(Long hospitalId);
    
    /**
     * Find all counters for a hospital
     */
    List<PharmacyCounter> findByHospitalIdOrderByOpenedAtDesc(Long hospitalId);
    
    /**
     * Find counters by hospital and date range
     */
    @Query("SELECT c FROM PharmacyCounter c WHERE c.hospital.id = :hospitalId " +
           "AND c.openedAt >= :startDate AND c.openedAt <= :endDate " +
           "ORDER BY c.openedAt DESC")
    List<PharmacyCounter> findByHospitalAndDateRange(
        @Param("hospitalId") Long hospitalId,
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );
}
