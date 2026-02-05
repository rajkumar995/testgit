package com.medidropbox.controller;

import com.medidropbox.dto.response.DeletionResponse;
import com.medidropbox.dto.response.PatientDataExportResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.PatientDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * Patient Data Controller
 * GDPR/DPDPA compliance endpoints: data export, deletion, portability
 */
@RestController
@RequestMapping("/api/v1/patients/data")
@Tag(name = "Patient Data", description = "GDPR/DPDPA compliance: data export, deletion, portability")
public class PatientDataController {
    
    private final PatientDataService patientDataService;
    
    public PatientDataController(PatientDataService patientDataService) {
        this.patientDataService = patientDataService;
    }
    
    @GetMapping("/export")
    @Operation(summary = "Export all patient data", 
               description = "GDPR Right to Access (Article 15). Exports all patient data from all hospitals and patient's own account.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientDataExportResponse> exportData(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(patientDataService.exportPatientData(globalPatientId));
    }
    
    @GetMapping("/export/portable")
    @Operation(summary = "Export data in portable format", 
               description = "GDPR Data Portability (Article 20). Exports data in machine-readable JSON format for transfer to another service.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> exportPortableData(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        String jsonData = patientDataService.exportPatientDataPortable(globalPatientId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", 
            "patient-data-export-" + globalPatientId + ".json");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(jsonData);
    }
    
    @DeleteMapping("/delete")
    @Operation(summary = "Delete patient data", 
               description = "GDPR Right to Deletion (Article 17). Archives data if legal obligation exists, otherwise permanently deletes.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<DeletionResponse> deleteData(
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        if (reason == null || reason.trim().isEmpty()) {
            reason = "Patient requested deletion";
        }
        return ResponseEntity.ok(patientDataService.deletePatientData(globalPatientId, reason));
    }
}
