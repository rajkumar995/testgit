package com.medidropbox.controller;

import com.medidropbox.dto.request.PatientRequest;
import com.medidropbox.dto.response.PatientResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.PatientService;
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
@RequestMapping("/api/v1/patients")
@Tag(name = "Patient Management", description = "Global patient management APIs (system-level, private)")
public class PatientController {
    
    private final PatientService patientService;
    
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }
    
    @PostMapping("/register")
    @Operation(summary = "Register/Claim patient", description = "Patient self-registration (no password required - uses OTP). If UNCLAIMED patient exists with same phone, claims it. Otherwise creates new CLAIMED patient. After registration, use OTP login.")
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.ok(patientService.registerPatient(request));
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get my profile", description = "Get current authenticated patient's profile. Uses globalPatientId from JWT token. No need to know patient ID.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(patientService.getPatientById(globalPatientId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Get patient details. PATIENT can only view their own profile.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('PATIENT', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // PATIENT can only view their own profile
        if (userDetails != null && userDetails.getRole() == Role.PATIENT) {
            Long globalPatientId = userDetails.getGlobalPatientId(); // Get global patient ID from user
            if (globalPatientId == null || !globalPatientId.equals(id)) {
                throw new RuntimeException("You can only view your own profile");
            }
        }
        return ResponseEntity.ok(patientService.getPatientById(id));
    }
    
    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get patient by phone", description = "Get patient details by phone number (STAFF/ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> getPatientByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(patientService.getPatientByPhone(phone));
    }
    
    @GetMapping
    @Operation(summary = "Get all patients", description = "Get all active patients (STAFF/ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update current authenticated patient's profile. Uses globalPatientId from JWT token. No need to know patient ID. Only provided fields will be updated (partial update).")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> updateMyProfile(
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(patientService.updatePatient(globalPatientId, request));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update patient", description = "Update patient details. PATIENT can only update their own profile.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('PATIENT', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // PATIENT can only update their own profile
        if (userDetails != null && userDetails.getRole() == Role.PATIENT) {
            Long globalPatientId = userDetails.getGlobalPatientId();
            if (globalPatientId == null || !globalPatientId.equals(id)) {
                throw new RuntimeException("You can only update your own profile");
            }
        }
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate patient", description = "Deactivate a patient (ADMIN/STAFF only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deactivatePatient(@PathVariable Long id) {
        patientService.deactivatePatient(id);
        return ResponseEntity.ok().build();
    }
}
