package com.medidropbox.repository;

import com.medidropbox.entity.Doctor;
import com.medidropbox.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {
    Optional<Doctor> findByIdAndIsActiveTrue(Long id);
    List<Doctor> findByHospitalAndIsActiveTrue(Hospital hospital);
    List<Doctor> findByHospitalIdAndIsActiveTrue(Long hospitalId);
    
    @Query("SELECT d FROM Doctor d WHERE d.hospital.id = :hospitalId AND d.isActive = true ORDER BY d.name")
    List<Doctor> findActiveDoctorsByHospital(@Param("hospitalId") Long hospitalId);
    
    /**
     * Find doctor by email (for linking with User)
     */
    Optional<Doctor> findByEmailAndIsActiveTrue(String email);
    
    /**
     * Find doctor by phone (for linking with User)
     */
    Optional<Doctor> findByPhoneAndIsActiveTrue(String phone);
}
