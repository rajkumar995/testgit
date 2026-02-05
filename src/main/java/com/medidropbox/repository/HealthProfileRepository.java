package com.medidropbox.repository;

import com.medidropbox.entity.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, Long> {
    
    Optional<HealthProfile> findByGlobalPatientId(Long globalPatientId);
    
    boolean existsByGlobalPatientId(Long globalPatientId);
    
    @Query("SELECT hp FROM HealthProfile hp WHERE hp.globalPatient.id = :patientId")
    Optional<HealthProfile> findByPatientId(@Param("patientId") Long patientId);

    /** Hospital context: profile uploaded by this hospital (for hospital-only fetch when no permission) */
    @Query("SELECT hp FROM HealthProfile hp WHERE hp.globalPatient.id = :patientId AND hp.uploadedByHospitalId = :hospitalId")
    Optional<HealthProfile> findByGlobalPatientIdAndUploadedByHospitalId(@Param("patientId") Long patientId, @Param("hospitalId") Long hospitalId);
}

