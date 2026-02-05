package com.medidropbox.repository;

import com.medidropbox.entity.LabReport;
import com.medidropbox.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabReportRepository extends JpaRepository<LabReport, Long> {
    
    /**
     * Find all reports by global patient ID
     */
    List<LabReport> findByGlobalPatientIdAndIsActiveTrueOrderByReportDateDesc(Long globalPatientId);
    
    /**
     * Find reports by patient ID and report type
     */
    List<LabReport> findByGlobalPatientIdAndReportTypeAndIsActiveTrueOrderByReportDateDesc(
        Long globalPatientId, 
        ReportType reportType
    );
    
    /**
     * Find report by ID and patient ID (for security)
     */
    Optional<LabReport> findByIdAndGlobalPatientIdAndIsActiveTrue(Long id, Long globalPatientId);
    
    /**
     * Find reports accessible to doctor (with permission check)
     */
    @Query("SELECT lr FROM LabReport lr " +
           "WHERE lr.globalPatientId = :patientId " +
           "AND lr.isActive = true " +
           "AND EXISTS (" +
           "  SELECT 1 FROM ReportPermission rp " +
           "  WHERE rp.globalPatientId = :patientId " +
           "  AND rp.isActive = true " +
           "  AND ((:doctorId IS NOT NULL AND rp.doctorId = :doctorId) OR " +
           "       (:hospitalId IS NOT NULL AND rp.hospitalId = :hospitalId) OR " +
           "       (:doctorId IS NOT NULL AND :hospitalId IS NOT NULL AND rp.hospitalId = :hospitalId)) " +
           "  AND (rp.expiresAt IS NULL OR rp.expiresAt > CURRENT_TIMESTAMP)" +
           ") " +
           "ORDER BY lr.reportDate DESC")
    List<LabReport> findAccessibleReportsForDoctor(
        @Param("patientId") Long patientId,
        @Param("doctorId") Long doctorId,
        @Param("hospitalId") Long hospitalId
    );
    
    /**
     * Count reports by patient ID
     */
    long countByGlobalPatientIdAndIsActiveTrue(Long globalPatientId);

    /**
     * Find reports for a patient uploaded by a specific hospital (hospital staff view)
     */
    List<LabReport> findByGlobalPatientIdAndUploadedByHospitalIdAndIsActiveTrueOrderByReportDateDesc(
            Long globalPatientId, Long uploadedByHospitalId);
}
