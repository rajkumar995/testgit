package com.medidropbox.repository;

import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.HospitalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HospitalSettingsRepository extends JpaRepository<HospitalSettings, Long> {
    /**
     * Find settings by hospital ID
     */
    Optional<HospitalSettings> findByHospitalId(Long hospitalId);
    
    /**
     * Find settings by hospital entity
     */
    Optional<HospitalSettings> findByHospital(Hospital hospital);
    
    /**
     * Check if settings exist for hospital
     */
    boolean existsByHospitalId(Long hospitalId);
}

