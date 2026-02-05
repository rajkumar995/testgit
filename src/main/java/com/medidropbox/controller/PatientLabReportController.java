package com.medidropbox.controller;

import com.medidropbox.dto.request.LabReportUploadRequest;
import com.medidropbox.dto.response.LabReportResponse;
import com.medidropbox.enums.ReportType;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.LabReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/patients/lab-reports")
@Tag(name = "Patient Lab Reports", description = "Patient lab report management APIs")
public class PatientLabReportController {
    
    private final LabReportService labReportService;
    
    public PatientLabReportController(LabReportService labReportService) {
        this.labReportService = labReportService;
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload lab report", description = "Upload a medical report (XRAY, CT_SCAN, BLOOD_TEST, etc.) to AWS S3. Uses global patient ID from JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<LabReportResponse> uploadReport(
            @RequestPart("file") MultipartFile file,
            @RequestPart("reportType") String reportType,
            @RequestPart("reportDate") String reportDate,
            @RequestPart(value = "doctorName", required = false) String doctorName,
            @RequestPart(value = "labName", required = false) String labName,
            @RequestPart(value = "aiSummary", required = false) String aiSummary,
            @RequestPart(value = "notes", required = false) String notes,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        
        LabReportUploadRequest request = new LabReportUploadRequest();
        request.setFile(file);
        request.setReportType(ReportType.valueOf(reportType));
        request.setReportDate(LocalDate.parse(reportDate));
        request.setDoctorName(doctorName);
        request.setLabName(labName);
        request.setAiSummary(aiSummary);
        request.setNotes(notes);
        
        return ResponseEntity.ok(labReportService.uploadReport(globalPatientId, request));
    }
    
    @GetMapping
    @Operation(summary = "Get my reports", description = "Get all lab reports for authenticated patient. Uses global patient ID from JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<LabReportResponse>> getMyReports(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(labReportService.getMyReports(globalPatientId));
    }
    
    @GetMapping("/type/{reportType}")
    @Operation(summary = "Get my reports by type", description = "Get lab reports filtered by type (XRAY, CT_SCAN, etc.)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<LabReportResponse>> getMyReportsByType(
            @PathVariable ReportType reportType,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(labReportService.getMyReportsByType(globalPatientId, reportType));
    }
    
    @GetMapping("/{reportId}")
    @Operation(summary = "Get report by ID", description = "Get specific lab report by ID (must own the report)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<LabReportResponse> getMyReportById(
            @PathVariable Long reportId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(labReportService.getMyReportById(globalPatientId, reportId));
    }
    
    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete report", description = "Soft delete a lab report (must own the report)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        labReportService.deleteReport(globalPatientId, reportId);
        return ResponseEntity.ok().build();
    }
}
