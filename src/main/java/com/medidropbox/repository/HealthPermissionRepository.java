package com.medidropbox.repository;

import com.medidropbox.entity.HealthPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthPermissionRepository extends JpaRepository<HealthPermission, Long> {

    Optional<HealthPermission> findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
            Long globalPatientId, Long doctorId, Long hospitalId);

    List<HealthPermission> findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(Long globalPatientId);

    @Query("SELECT COUNT(hp) > 0 FROM HealthPermission hp " +
           "WHERE hp.globalPatientId = :patientId " +
           "AND hp.isActive = true " +
           "AND ((:doctorId IS NOT NULL AND hp.doctorId = :doctorId) OR " +
           "     (:hospitalId IS NOT NULL AND hp.hospitalId = :hospitalId) OR " +
           "     (:doctorId IS NOT NULL AND :hospitalId IS NOT NULL AND hp.hospitalId = :hospitalId)) " +
           "AND (hp.expiresAt IS NULL OR hp.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasPermission(
        @Param("patientId") Long patientId,
        @Param("doctorId") Long doctorId,
        @Param("hospitalId") Long hospitalId
    );
}
