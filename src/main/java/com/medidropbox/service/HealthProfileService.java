package com.medidropbox.service;

import com.medidropbox.dto.request.HealthProfileRequest;
import com.medidropbox.dto.request.VitalsHistoryRequest;
import com.medidropbox.dto.request.VitalsRecordRequest;
import com.medidropbox.dto.response.BmiReportResponse;
import com.medidropbox.dto.response.HealthProfileResponse;
import com.medidropbox.dto.response.LatestVitalsResponse;
import com.medidropbox.dto.response.VitalsRecordResponse;

import java.util.List;

/**
 * Service for Health Profile and Vitals Management
 */
public interface HealthProfileService {
    
    /**
     * Create or update health profile (fixed values)
     * One profile per patient - updating replaces old values
     */
    HealthProfileResponse createOrUpdateHealthProfile(Long globalPatientId, HealthProfileRequest request);
    
    /**
     * Get health profile by patient ID
     */
    HealthProfileResponse getHealthProfile(Long globalPatientId);
    
    /**
     * Create vitals record (history-based)
     * Each submission creates a new record
     */
    VitalsRecordResponse createVitalsRecord(Long globalPatientId, VitalsRecordRequest request);
    
    /**
     * Update vitals record
     * Only the owner (patient) can update
     */
    VitalsRecordResponse updateVitalsRecord(Long globalPatientId, Long recordId, VitalsRecordRequest request);
    
    /**
     * Delete vitals record
     * Only the owner (patient) can delete
     */
    void deleteVitalsRecord(Long globalPatientId, Long recordId);
    
    /**
     * Get latest vitals for each type
     * Returns most recent value for weight, glucose, and blood pressure
     */
    LatestVitalsResponse getLatestVitals(Long globalPatientId);
    
    /**
     * Get vitals history
     * Filter by date range and/or vital type
     */
    List<VitalsRecordResponse> getVitalsHistory(Long globalPatientId, VitalsHistoryRequest request);
    
    /**
     * Get BMI report with inspirational quotes
     * Calculates BMI dynamically using latest weight and stored height
     */
    BmiReportResponse getBmiReport(Long globalPatientId);

    // ---------- Hospital context (same flow as reports: hospital can add; fetch only own unless patient approved) ----------

    /**
     * Get health profile for hospital: if hospital has HealthPermission return full profile; else only if profile was uploaded by this hospital.
     */
    HealthProfileResponse getHealthProfileForHospitalContext(Long globalPatientId, Long hospitalId);

    /**
     * Hospital/doctor creates or updates health profile for patient; sets uploadedByHospitalId/uploadedByDoctorId.
     */
    HealthProfileResponse createOrUpdateHealthProfileForHospital(Long globalPatientId, Long hospitalId, Long doctorId, HealthProfileRequest request);

    /**
     * Get latest vitals for hospital: if has VitalsPermission return all; else only vitals uploaded by this hospital.
     */
    LatestVitalsResponse getLatestVitalsForHospitalContext(Long globalPatientId, Long hospitalId);

    /**
     * Get vitals history for hospital: same permission rule.
     */
    List<VitalsRecordResponse> getVitalsHistoryForHospitalContext(Long globalPatientId, Long hospitalId, VitalsHistoryRequest request);

    /**
     * Hospital/doctor creates vitals record for patient; sets uploadedByHospitalId/uploadedByDoctorId.
     */
    VitalsRecordResponse createVitalsRecordForHospital(Long globalPatientId, Long hospitalId, Long doctorId, VitalsRecordRequest request);

    /**
     * Get BMI report for hospital: if has HealthPermission or VitalsPermission return full; else from hospital-uploaded data only, or throw if insufficient.
     */
    BmiReportResponse getBmiReportForHospitalContext(Long globalPatientId, Long hospitalId);
}

