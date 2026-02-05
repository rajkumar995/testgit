package com.medidropbox.repository;

import com.medidropbox.entity.VitalsPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalsPermissionRepository extends JpaRepository<VitalsPermission, Long> {

    Optional<VitalsPermission> findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
            Long globalPatientId, Long doctorId, Long hospitalId);

    List<VitalsPermission> findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(Long globalPatientId);

    @Query("SELECT COUNT(vp) > 0 FROM VitalsPermission vp " +
           "WHERE vp.globalPatientId = :patientId " +
           "AND vp.isActive = true " +
           "AND ((:doctorId IS NOT NULL AND vp.doctorId = :doctorId) OR " +
           "     (:hospitalId IS NOT NULL AND vp.hospitalId = :hospitalId) OR " +
           "     (:doctorId IS NOT NULL AND :hospitalId IS NOT NULL AND vp.hospitalId = :hospitalId)) " +
           "AND (vp.expiresAt IS NULL OR vp.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasPermission(
        @Param("patientId") Long patientId,
        @Param("doctorId") Long doctorId,
        @Param("hospitalId") Long hospitalId
    );
}
