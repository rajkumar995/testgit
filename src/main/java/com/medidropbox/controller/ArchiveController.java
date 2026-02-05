package com.medidropbox.controller;

import com.medidropbox.dto.response.PatientDataExportResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.PatientDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Archive Controller
 * Admin endpoints for retrieving archived patient data (for government/legal requests)
 */
@RestController
@RequestMapping("/api/v1/admin/archive")
@Tag(name = "Archive", description = "Retrieve archived patient data for legal/government requests")
public class ArchiveController {
    
    private final PatientDataService patientDataService;
    
    public ArchiveController(PatientDataService patientDataService) {
        this.patientDataService = patientDataService;
    }
    
    @GetMapping("/patients/{originalGlobalPatientId}")
    @Operation(summary = "Get archived patient data", 
               description = "Retrieve archived patient data for government/legal requests. Requires admin authorization.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN')")
    public ResponseEntity<PatientDataExportResponse> getArchivedData(
            @PathVariable Long originalGlobalPatientId,
            @RequestHeader(value = "X-Request-Authority", required = false) String requestAuthority,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        // Log access for audit
        // auditLogService.logDataAccess(userDetails.getId(), "ArchivedPatientData", originalGlobalPatientId, "GOVERNMENT_REQUEST");
        
        PatientDataExportResponse data = patientDataService.getArchivedPatientData(originalGlobalPatientId);
        return ResponseEntity.ok(data);
    }
}
