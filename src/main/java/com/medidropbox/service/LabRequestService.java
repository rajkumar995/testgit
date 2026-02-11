package com.medidropbox.service;

import com.medidropbox.dto.request.LabRequestCreateRequest;
import com.medidropbox.dto.request.LabRequestUpdateRequest;
import com.medidropbox.dto.response.LabRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LabRequestService {
    
    /**
     * Create lab request (by doctor or hospital)
     */
    LabRequestResponse createRequest(Long requestingHospitalId, Long requestingDoctorId, LabRequestCreateRequest request);
    
    /**
     * Get all requests for a lab (assigned to this lab)
     */
    Page<LabRequestResponse> getLabRequests(Long labId, Pageable pageable);
    
    /**
     * Get requests by status for a lab
     */
    Page<LabRequestResponse> getLabRequestsByStatus(Long labId, String status, Pageable pageable);
    
    /**
     * Get requests created by a hospital
     */
    Page<LabRequestResponse> getHospitalRequests(Long hospitalId, Pageable pageable);
    
    /**
     * Get requests created by a doctor
     */
    Page<LabRequestResponse> getDoctorRequests(Long doctorId, Pageable pageable);
    
    /**
     * Get requests for a patient
     */
    Page<LabRequestResponse> getPatientRequests(Long patientId, Pageable pageable);
    
    /**
     * Get request by ID
     */
    LabRequestResponse getRequestById(Long requestId);
    
    /**
     * Get request by barcode
     */
    LabRequestResponse getRequestByBarcode(String barcode);
    
    /**
     * Update request status and details
     */
    LabRequestResponse updateRequest(Long requestId, LabRequestUpdateRequest request);
    
    /**
     * Mark sample as received
     */
    LabRequestResponse markSampleReceived(Long requestId, String receivedBy);
    
    /**
     * Start test
     */
    LabRequestResponse startTest(Long requestId);
    
    /**
     * Complete test and link report
     */
    LabRequestResponse completeTest(Long requestId, Long labReportId);
    
    /**
     * Cancel request
     */
    void cancelRequest(Long requestId, String reason);
    
    /**
     * Delete request (soft delete)
     */
    void deleteRequest(Long requestId);
}
