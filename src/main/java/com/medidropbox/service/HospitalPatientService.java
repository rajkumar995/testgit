package com.medidropbox.service;

import com.medidropbox.dto.request.PatientRequest;
import com.medidropbox.dto.response.PatientResponse;
import com.medidropbox.entity.HospitalPatient;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for Hospital-scoped patient management
 * Hospitals create and manage their own patient records
 * Each hospital maintains separate patient records
 */
public interface HospitalPatientService {
    /**
     * Create or get hospital patient
     * If patient doesn't exist in this hospital, creates both:
     * 1. HospitalPatient record (hospital-specific)
     * 2. GlobalPatient record (UNCLAIMED, if doesn't exist)
     * 
     * @param dataConsent If true, copies full data (address, Aadhar, ABHA, emergency contacts) from GlobalPatient
     *                    If false or null, only saves basic data (name, phone, email)
     */
    HospitalPatient createOrGetHospitalPatient(Long hospitalId, String phone, String email, String fullName, Boolean dataConsent);

    /**
     * Create or get hospital patient for "book for friend" flow. Uses only hospital-scoped data; no global data copy.
     * Friend's name, phone, DOB, email, description stored in Hospital Patient. Global Patient (UNCLAIMED) created only for FK.
     */
    HospitalPatient createOrGetHospitalPatientForBookForFriend(Long hospitalId, String name, String phone,
                                                                LocalDate dateOfBirth, String email, String description);
    
    /**
     * Create hospital patient with full data from PatientRequest
     * Saves all fields including address, aadharId, abhaId, and emergency contacts
     */
    PatientResponse createHospitalPatientWithFullData(Long hospitalId, PatientRequest request);
    
    /**
     * Get hospital patient by ID (hospital-scoped)
     */
    HospitalPatient getHospitalPatientById(Long hospitalId, Long hospitalPatientId);
    
    /**
     * Get hospital patient by ID and return as PatientResponse
     */
    PatientResponse getHospitalPatientByIdAsResponse(Long hospitalId, Long hospitalPatientId);
    
    /**
     * Get hospital patient by phone (hospital-scoped)
     */
    HospitalPatient getHospitalPatientByPhone(Long hospitalId, String phone);
    
    /**
     * Get hospital patient by phone and return as PatientResponse
     */
    PatientResponse getHospitalPatientByPhoneAsResponse(Long hospitalId, String phone);
    
    /**
     * Get all patients for a hospital
     */
    List<PatientResponse> getHospitalPatients(Long hospitalId);
    
    /**
     * Update hospital patient (hospital staff can update)
     */
    PatientResponse updateHospitalPatient(Long hospitalId, Long hospitalPatientId, PatientRequest request);
    
    /**
     * Deactivate hospital patient
     */
    void deactivateHospitalPatient(Long hospitalId, Long hospitalPatientId);
    
    /**
     * Get global patient ID from hospital patient (for linking bookings)
     */
    Long getGlobalPatientId(Long hospitalId, Long hospitalPatientId);
}
