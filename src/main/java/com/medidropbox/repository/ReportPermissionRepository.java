package com.medidropbox.repository;

import com.medidropbox.entity.ReportPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportPermissionRepository extends JpaRepository<ReportPermission, Long> {
    
    /**
     * Find all permissions granted by a patient
     */
    List<ReportPermission> findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(Long globalPatientId);
    
    /**
     * Find permission by patient, doctor, and hospital
     */
    Optional<ReportPermission> findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
        Long globalPatientId,
        Long doctorId,
        Long hospitalId
    );
    
    /**
     * Check if doctor has permission to access patient reports
     */
    @Query("SELECT COUNT(rp) > 0 FROM ReportPermission rp " +
           "WHERE rp.globalPatientId = :patientId " +
           "AND rp.isActive = true " +
           "AND ((:doctorId IS NOT NULL AND rp.doctorId = :doctorId) OR " +
           "     (:hospitalId IS NOT NULL AND rp.hospitalId = :hospitalId) OR " +
           "     (:doctorId IS NOT NULL AND :hospitalId IS NOT NULL AND rp.hospitalId = :hospitalId)) " +
           "AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasPermission(
        @Param("patientId") Long patientId,
        @Param("doctorId") Long doctorId,
        @Param("hospitalId") Long hospitalId
    );
    
    /**
     * Find permissions for a specific doctor
     */
    @Query("SELECT rp FROM ReportPermission rp " +
           "WHERE rp.doctorId = :doctorId " +
           "AND rp.isActive = true " +
           "AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)")
    List<ReportPermission> findActivePermissionsForDoctor(@Param("doctorId") Long doctorId);
    
    /**
     * Find permissions for a specific hospital
     */
    @Query("SELECT rp FROM ReportPermission rp " +
           "WHERE rp.hospitalId = :hospitalId " +
           "AND rp.isActive = true " +
           "AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)")
    List<ReportPermission> findActivePermissionsForHospital(@Param("hospitalId") Long hospitalId);
}
