package com.medidropbox.controller;

import com.medidropbox.dto.request.HealthProfileRequest;
import com.medidropbox.dto.request.VitalsHistoryRequest;
import com.medidropbox.dto.request.VitalsRecordRequest;
import com.medidropbox.dto.response.BmiReportResponse;
import com.medidropbox.dto.response.HealthProfileResponse;
import com.medidropbox.dto.response.LatestVitalsResponse;
import com.medidropbox.dto.response.VitalsRecordResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HealthProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-profile")
@Tag(name = "Health Profile & Vitals", description = "Patient health profile and vitals tracking APIs")
public class HealthProfileController {
    
    private final HealthProfileService healthProfileService;
    
    public HealthProfileController(HealthProfileService healthProfileService) {
        this.healthProfileService = healthProfileService;
    }
    
    @PostMapping("/profile")
    @Operation(summary = "Create or update health profile", 
               description = "Create or update fixed health profile (height, blood group, gender, date of birth). " +
                           "One profile per patient. Updating replaces old values.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<HealthProfileResponse> createOrUpdateHealthProfile(
            @Valid @RequestBody HealthProfileRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(healthProfileService.createOrUpdateHealthProfile(globalPatientId, request));
    }
    
    @GetMapping("/profile")
    @Operation(summary = "Get health profile", 
               description = "Get current authenticated patient's health profile")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<HealthProfileResponse> getHealthProfile(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(healthProfileService.getHealthProfile(globalPatientId));
    }
    
    @PostMapping("/vitals")
    @Operation(summary = "Create vitals record", 
               description = "Create a new vitals record (history-based). At least one vital value is required. " +
                           "Each submission creates a new record (preserves full history).")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<VitalsRecordResponse> createVitalsRecord(
            @Valid @RequestBody VitalsRecordRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(healthProfileService.createVitalsRecord(globalPatientId, request));
    }
    
    @PutMapping("/vitals/{recordId}")
    @Operation(summary = "Update vitals record", 
               description = "Update a specific vitals entry. Only the owner (patient) can update their records.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<VitalsRecordResponse> updateVitalsRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody VitalsRecordRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(healthProfileService.updateVitalsRecord(globalPatientId, recordId, request));
    }
    
    @DeleteMapping("/vitals/{recordId}")
    @Operation(summary = "Delete vitals record", 
               description = "Delete a specific vitals entry. Only the owner (patient) can delete their records.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> deleteVitalsRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        healthProfileService.deleteVitalsRecord(globalPatientId, recordId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/vitals/latest")
    @Operation(summary = "Get latest vitals", 
               description = "Get the most recent value for each vital (weight, glucose, blood pressure). " +
                           "Includes BMI calculation if height and weight are available.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<LatestVitalsResponse> getLatestVitals(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(healthProfileService.getLatestVitals(globalPatientId));
    }
    
    @PostMapping("/vitals/history")
    @Operation(summary = "Get vitals history", 
               description = "Get full vitals history with optional filters by date range and/or vital type.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<VitalsRecordResponse>> getVitalsHistory(
            @RequestBody(required = false) VitalsHistoryRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        if (request == null) {
            request = new VitalsHistoryRequest();
        }
        return ResponseEntity.ok(healthProfileService.getVitalsHistory(globalPatientId, request));
    }
    
    @GetMapping("/bmi-report")
    @Operation(summary = "Get BMI report", 
               description = "Get comprehensive BMI report with calculation, category, description, " +
                           "inspirational quote, and health recommendations. BMI is calculated dynamically " +
                           "using latest weight and stored height.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BmiReportResponse> getBmiReport(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(healthProfileService.getBmiReport(globalPatientId));
    }
}

