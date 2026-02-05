package com.medidropbox.repository;

import com.medidropbox.entity.PermissionRequest;
import com.medidropbox.enums.PermissionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRequestRepository extends JpaRepository<PermissionRequest, Long> {

    List<PermissionRequest> findByGlobalPatientIdOrderByRequestedAtDesc(Long globalPatientId);

    List<PermissionRequest> findByGlobalPatientIdAndStatusOrderByRequestedAtDesc(
            Long globalPatientId, PermissionRequestStatus status);

    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.globalPatientId = :patientId " +
           "AND (pr.doctorId = :doctorId OR (:doctorId IS NULL AND pr.doctorId IS NULL)) " +
           "AND (pr.hospitalId = :hospitalId OR (:hospitalId IS NULL AND pr.hospitalId IS NULL)) " +
           "AND pr.status = 'PENDING' ORDER BY pr.requestedAt DESC")
    List<PermissionRequest> findPendingByPatientAndDoctorAndHospital(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("hospitalId") Long hospitalId);

    @Query("SELECT pr FROM PermissionRequest pr WHERE " +
           "(:doctorId IS NOT NULL AND pr.doctorId = :doctorId) OR " +
           "(:hospitalId IS NOT NULL AND pr.hospitalId = :hospitalId) " +
           "ORDER BY pr.requestedAt DESC")
    List<PermissionRequest> findMyRequests(@Param("doctorId") Long doctorId, @Param("hospitalId") Long hospitalId);

    @Query("SELECT pr FROM PermissionRequest pr WHERE pr.globalPatientId = :patientId AND " +
           "((:doctorId IS NOT NULL AND pr.doctorId = :doctorId) OR (:hospitalId IS NOT NULL AND pr.hospitalId = :hospitalId)) " +
           "ORDER BY pr.requestedAt DESC")
    List<PermissionRequest> findMyRequestsForPatient(
            @Param("patientId") Long patientId,
            @Param("doctorId") Long doctorId,
            @Param("hospitalId") Long hospitalId);
}
