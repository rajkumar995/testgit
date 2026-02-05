package com.medidropbox.controller;

import com.medidropbox.dto.response.LabReportResponse;
import com.medidropbox.enums.ReportType;
import com.medidropbox.repository.DoctorRepository;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.LabReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/lab-reports")
@Tag(name = "Doctor Lab Reports", description = "Doctor access to patient lab reports (requires permission)")
public class DoctorLabReportController {
    
    private final LabReportService labReportService;
    private final DoctorRepository doctorRepository;
    
    public DoctorLabReportController(LabReportService labReportService, DoctorRepository doctorRepository) {
        this.labReportService = labReportService;
        this.doctorRepository = doctorRepository;
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient reports", description = "Get all lab reports for a patient by entering patient ID. Requires permission from patient.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<List<LabReportResponse>> getPatientReports(
            @PathVariable Long patientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        // Try to find doctor ID by email/phone from username
        Long doctorId = findDoctorIdByUsername(userDetails.getUsername());
        Long hospitalId = userDetails.getHospitalId();
        
        if (doctorId == null && hospitalId == null) {
            throw new RuntimeException("Doctor or hospital ID not found. Please ensure you are linked to a doctor or hospital.");
        }
        
        return ResponseEntity.ok(labReportService.getPatientReports(patientId, doctorId, hospitalId));
    }
    
    @GetMapping("/patient/{patientId}/type/{reportType}")
    @Operation(summary = "Get patient reports by type", description = "Get patient lab reports filtered by type. Requires permission.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<List<LabReportResponse>> getPatientReportsByType(
            @PathVariable Long patientId,
            @PathVariable ReportType reportType,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        Long doctorId = findDoctorIdByUsername(userDetails.getUsername());
        Long hospitalId = userDetails.getHospitalId();
        
        if (doctorId == null && hospitalId == null) {
            throw new RuntimeException("Doctor or hospital ID not found. Please ensure you are linked to a doctor or hospital.");
        }
        
        return ResponseEntity.ok(labReportService.getPatientReportsByType(patientId, reportType, doctorId, hospitalId));
    }
    
    private Long findDoctorIdByUsername(String username) {
        // Try to find doctor by email
        return doctorRepository.findByEmailAndIsActiveTrue(username)
                .map(d -> d.getId())
                .orElseGet(() -> 
                    // Try to find by phone
                    doctorRepository.findByPhoneAndIsActiveTrue(username)
                            .map(d -> d.getId())
                            .orElse(null)
                );
    }
}
