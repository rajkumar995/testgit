package com.medidropbox.service;

import com.medidropbox.dto.response.DeletionResponse;
import com.medidropbox.dto.response.PatientDataExportResponse;

/**
 * Patient Data Service
 * Handles GDPR/DPDPA compliance: data export, deletion, portability
 */
public interface PatientDataService {
    
    /**
     * Export all patient data (GDPR Right to Access - Article 15)
     * Collects data from all hospitals and patient's own account
     */
    PatientDataExportResponse exportPatientData(Long globalPatientId);
    
    /**
     * Delete patient data (GDPR Right to Deletion - Article 17)
     * Archives data if legal obligation exists, otherwise permanently deletes
     */
    DeletionResponse deletePatientData(Long globalPatientId, String reason);
    
    /**
     * Export patient data in machine-readable format (GDPR Data Portability - Article 20)
     */
    String exportPatientDataPortable(Long globalPatientId);
    
    /**
     * Get archived patient data (for government/legal requests)
     */
    PatientDataExportResponse getArchivedPatientData(Long originalGlobalPatientId);
}
