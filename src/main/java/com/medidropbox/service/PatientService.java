package com.medidropbox.service;

import com.medidropbox.dto.request.PatientRequest;
import com.medidropbox.dto.response.PatientResponse;
import com.medidropbox.entity.GlobalPatient;

import java.util.List;
import java.util.Optional;

/**
 * Service for Global Patient management (system-level, private)
 * Handles patient registration, claiming, and profile management
 */
public interface PatientService {
    /**
     * Create or get global patient (used internally by hospital when creating patient)
     * Creates UNCLAIMED patient if doesn't exist
     */
    GlobalPatient createOrGetGlobalPatient(String phone, String email, String fullName);
    
    /**
     * Get global patient by ID (internal use)
     */
    Optional<GlobalPatient> getGlobalPatientById(Long id);
    
    /**
     * Get global patient by phone (internal use)
     */
    Optional<GlobalPatient> getGlobalPatientByPhone(String phone);
    
    /**
     * Register/Claim patient - Patient self-registers (no password, uses OTP)
     * If UNCLAIMED patient exists with same phone, claims it
     * Otherwise creates new CLAIMED patient
     */
    PatientResponse registerPatient(PatientRequest request);
    
    /**
     * Get patient by ID (for authenticated patient viewing their own profile)
     */
    PatientResponse getPatientById(Long id);
    
    /**
     * Get patient by phone (for lookup)
     */
    PatientResponse getPatientByPhone(String phone);
    
    /**
     * Get all active patients (ADMIN only)
     */
    List<PatientResponse> getAllPatients();
    
    /**
     * Update patient profile (patient can update their own profile)
     */
    PatientResponse updatePatient(Long id, PatientRequest request);
    
    /**
     * Deactivate patient (ADMIN only)
     */
    void deactivatePatient(Long id);
    
    /**
     * Claim existing UNCLAIMED patient (when patient registers with existing phone)
     * No password needed - uses OTP authentication
     */
    GlobalPatient claimPatient(String phone);
}
