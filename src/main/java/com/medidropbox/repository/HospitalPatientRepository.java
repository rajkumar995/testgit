package com.medidropbox.repository;

import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.HospitalPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalPatientRepository extends JpaRepository<HospitalPatient, Long> {
    Optional<HospitalPatient> findByHospitalAndPhone(Hospital hospital, String phone);
    Optional<HospitalPatient> findByHospitalIdAndPhone(Long hospitalId, String phone);
    List<HospitalPatient> findByHospital(Hospital hospital);
    List<HospitalPatient> findByHospitalId(Long hospitalId);
    List<HospitalPatient> findByHospitalIdAndIsActiveTrue(Long hospitalId);
    List<HospitalPatient> findByGlobalPatient(GlobalPatient globalPatient);
    List<HospitalPatient> findByGlobalPatientId(Long globalPatientId);
    boolean existsByHospitalAndPhone(Hospital hospital, String phone);
    boolean existsByHospitalIdAndPhone(Long hospitalId, String phone);
    
    // Find by Aadhar ID within a hospital
    Optional<HospitalPatient> findByHospitalIdAndAadharId(Long hospitalId, String aadharId);
    
    // Find by ABHA ID within a hospital
    Optional<HospitalPatient> findByHospitalIdAndAbhaId(Long hospitalId, String abhaId);
    
    // Find by any identifier (phone, aadharId, or abhaId) within a hospital
    @Query("SELECT hp FROM HospitalPatient hp WHERE hp.hospital.id = :hospitalId AND hp.isActive = true AND " +
           "(hp.phone = :identifier OR hp.aadharId = :identifier OR hp.abhaId = :identifier)")
    Optional<HospitalPatient> findByHospitalIdAndAnyIdentifier(@Param("hospitalId") Long hospitalId, 
                                                               @Param("identifier") String identifier);
    
    @Query("SELECT hp FROM HospitalPatient hp WHERE hp.hospital.id = :hospitalId AND hp.isActive = true")
    List<HospitalPatient> findActivePatientsByHospital(@Param("hospitalId") Long hospitalId);
}
