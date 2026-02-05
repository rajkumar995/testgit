package com.medidropbox.repository;

import com.medidropbox.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long>, JpaSpecificationExecutor<Hospital> {
    Optional<Hospital> findByIdAndIsActiveTrue(Long id);
    List<Hospital> findByIsActiveTrue();
    
    @Query("SELECT h FROM Hospital h WHERE h.isActive = true ORDER BY h.name")
    List<Hospital> findAllActiveHospitals();
}
