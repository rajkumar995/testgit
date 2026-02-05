package com.medidropbox.controller;

import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.repository.GlobalPatientRepository;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Legal Hold Controller
 * Manages legal holds on patient data (prevents deletion)
 */
@RestController
@RequestMapping("/api/v1/admin/legal-hold")
@Tag(name = "Legal Hold", description = "Manage legal holds on patient data")
public class LegalHoldController {
    
    private final GlobalPatientRepository globalPatientRepository;
    private final AuditLogService auditLogService;
    
    public LegalHoldController(GlobalPatientRepository globalPatientRepository,
                              AuditLogService auditLogService) {
        this.globalPatientRepository = globalPatientRepository;
        this.auditLogService = auditLogService;
    }
    
    @PostMapping("/patients/{globalPatientId}")
    @Operation(summary = "Place legal hold on patient data", 
               description = "Prevents deletion of patient data. Required for legal investigations, court orders, etc.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> placeLegalHold(
            @PathVariable Long globalPatientId,
            @RequestParam String reason,
            @RequestParam(required = false) String expiresAt,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patient.setLegalHold(true);
        patient.setLegalHoldReason(reason);
        patient.setLegalHoldPlacedBy(userDetails.getUsername());
        patient.setLegalHoldPlacedAt(LocalDateTime.now());
        
        if (expiresAt != null && !expiresAt.isEmpty()) {
            patient.setLegalHoldExpiresAt(LocalDateTime.parse(expiresAt));
        }
        
        globalPatientRepository.save(patient);
        
        // Audit log
        auditLogService.logPermissionChange(
            userDetails.getUserId(),
            globalPatientId,
            "PatientData",
            "LEGAL_HOLD_PLACED"
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Legal hold placed successfully");
        response.put("globalPatientId", globalPatientId);
        response.put("reason", reason);
        response.put("placedBy", userDetails.getUsername());
        response.put("placedAt", patient.getLegalHoldPlacedAt());
        response.put("expiresAt", patient.getLegalHoldExpiresAt());
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/patients/{globalPatientId}")
    @Operation(summary = "Remove legal hold from patient data")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> removeLegalHold(
            @PathVariable Long globalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        patient.setLegalHold(false);
        patient.setLegalHoldReason(null);
        patient.setLegalHoldExpiresAt(null);
        patient.setLegalHoldPlacedBy(null);
        patient.setLegalHoldPlacedAt(null);
        
        globalPatientRepository.save(patient);
        
        // Audit log
        auditLogService.logPermissionChange(
            userDetails.getUserId(),
            globalPatientId,
            "PatientData",
            "LEGAL_HOLD_REMOVED"
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Legal hold removed successfully");
        response.put("globalPatientId", globalPatientId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/patients/{globalPatientId}")
    @Operation(summary = "Check legal hold status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PATIENT')")
    public ResponseEntity<Map<String, Object>> getLegalHoldStatus(
            @PathVariable Long globalPatientId) {
        
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("globalPatientId", globalPatientId);
        response.put("legalHold", patient.getLegalHold());
        response.put("reason", patient.getLegalHoldReason());
        response.put("placedBy", patient.getLegalHoldPlacedBy());
        response.put("placedAt", patient.getLegalHoldPlacedAt());
        response.put("expiresAt", patient.getLegalHoldExpiresAt());
        
        return ResponseEntity.ok(response);
    }
}
