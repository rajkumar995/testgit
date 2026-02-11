package com.medidropbox.repository;

import com.medidropbox.entity.LabRequest;
import com.medidropbox.enums.LabRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabRequestRepository extends JpaRepository<LabRequest, Long> {
    
    /**
     * Find by barcode
     */
    Optional<LabRequest> findByBarcodeAndIsActiveTrue(String barcode);
    
    /**
     * Find all requests for a specific lab/hospital
     */
    Page<LabRequest> findByAssignedLabIdAndIsActiveTrueOrderByCreatedAtDesc(Long labId, Pageable pageable);
    
    /**
     * Find requests by status for a lab
     */
    Page<LabRequest> findByAssignedLabIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
        Long labId, 
        LabRequestStatus status, 
        Pageable pageable
    );
    
    /**
     * Find requests by requesting hospital
     */
    Page<LabRequest> findByRequestingHospitalIdAndIsActiveTrueOrderByCreatedAtDesc(Long hospitalId, Pageable pageable);
    
    /**
     * Find requests by requesting doctor
     */
    Page<LabRequest> findByRequestingDoctorIdAndIsActiveTrueOrderByCreatedAtDesc(Long doctorId, Pageable pageable);
    
    /**
     * Find requests for a patient
     */
    Page<LabRequest> findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(Long patientId, Pageable pageable);
    
    /**
     * Find requests by patient and status
     */
    List<LabRequest> findByPatientIdAndStatusAndIsActiveTrue(Long patientId, LabRequestStatus status);
    
    /**
     * Count pending requests for a lab
     */
    @Query("SELECT COUNT(lr) FROM LabRequest lr WHERE lr.assignedLab.id = :labId AND lr.status IN :statuses AND lr.isActive = true")
    Long countByLabIdAndStatusIn(@Param("labId") Long labId, @Param("statuses") List<LabRequestStatus> statuses);
}
