package com.medidropbox.service;

import com.medidropbox.dto.request.GrantReportPermissionRequest;
import com.medidropbox.dto.request.LabReportUploadRequest;
import com.medidropbox.dto.response.LabReportResponse;
import com.medidropbox.dto.response.ReportPermissionResponse;
import com.medidropbox.enums.ReportType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Lab Report Service
 * Handles patient report uploads, viewing, and permission management
 */
public interface LabReportService {
    
    /**
     * Upload lab report (Patient only)
     * @param globalPatientId Global patient ID from JWT
     * @param request Upload request with file
     * @return Lab report response
     */
    LabReportResponse uploadReport(Long globalPatientId, LabReportUploadRequest request);
    
    /**
     * Get all reports for patient (Patient only)
     * @param globalPatientId Global patient ID from JWT
     * @return List of lab reports
     */
    List<LabReportResponse> getMyReports(Long globalPatientId);
    
    /**
     * Get reports by type (Patient only)
     * @param globalPatientId Global patient ID from JWT
     * @param reportType Report type filter
     * @return List of lab reports
     */
    List<LabReportResponse> getMyReportsByType(Long globalPatientId, ReportType reportType);
    
    /**
     * Get report by ID (Patient only - must own the report)
     * @param globalPatientId Global patient ID from JWT
     * @param reportId Report ID
     * @return Lab report response
     */
    LabReportResponse getMyReportById(Long globalPatientId, Long reportId);
    
    /**
     * Delete report (Patient only - soft delete)
     * @param globalPatientId Global patient ID from JWT
     * @param reportId Report ID
     */
    void deleteReport(Long globalPatientId, Long reportId);
    
    /**
     * Grant permission to doctor/hospital (Patient only)
     * @param globalPatientId Global patient ID from JWT
     * @param request Permission request
     * @return Permission response
     */
    ReportPermissionResponse grantPermission(Long globalPatientId, GrantReportPermissionRequest request);
    
    /**
     * Revoke permission (Patient only)
     * @param globalPatientId Global patient ID from JWT
     * @param permissionId Permission ID
     */
    void revokePermission(Long globalPatientId, Long permissionId);
    
    /**
     * Get all permissions granted by patient (Patient only)
     * @param globalPatientId Global patient ID from JWT
     * @return List of permissions
     */
    List<ReportPermissionResponse> getMyPermissions(Long globalPatientId);
    
    /**
     * Get patient reports by patient ID (Doctor only - requires permission)
     * @param patientId Global patient ID
     * @param doctorId Doctor ID from JWT
     * @param hospitalId Hospital ID from JWT
     * @return List of accessible reports
     */
    List<LabReportResponse> getPatientReports(Long patientId, Long doctorId, Long hospitalId);
    
    /**
     * Get patient reports by type (Doctor only - requires permission)
     * @param patientId Global patient ID
     * @param reportType Report type filter
     * @param doctorId Doctor ID from JWT
     * @param hospitalId Hospital ID from JWT
     * @return List of accessible reports
     */
    List<LabReportResponse> getPatientReportsByType(
        Long patientId, 
        ReportType reportType, 
        Long doctorId, 
        Long hospitalId
    );

    /**
     * Upload report for a patient by hospital staff (report is scoped to that hospital).
     * @param globalPatientId Patient's global ID
     * @param hospitalId Staff's hospital ID (must match token)
     * @param request Upload request
     * @return Saved report response
     */
    LabReportResponse uploadReportForPatientByHospital(Long globalPatientId, Long hospitalId, LabReportUploadRequest request);

    /**
     * Get reports for a patient uploaded by a specific hospital (hospital staff see only their uploads).
     * @param globalPatientId Patient's global ID
     * @param hospitalId Staff's hospital ID (must match token)
     * @return List of reports uploaded by this hospital
     */
    List<LabReportResponse> getReportsForPatientByHospital(Long globalPatientId, Long hospitalId);

    /**
     * Get patient reports for hospital context: if hospital has ReportPermission, return all reports;
     * otherwise return only reports uploaded by this hospital.
     */
    List<LabReportResponse> getPatientReportsForHospitalContext(Long globalPatientId, Long hospitalId);
}
