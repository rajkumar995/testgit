package com.medidropbox.controller;

import com.medidropbox.dto.request.HealthProfileRequest;
import com.medidropbox.dto.request.LabReportUploadRequest;
import com.medidropbox.dto.request.PatientRequest;
import com.medidropbox.dto.request.VitalsHistoryRequest;
import com.medidropbox.dto.request.VitalsRecordRequest;
import com.medidropbox.dto.response.BmiReportResponse;
import com.medidropbox.dto.response.HealthProfileResponse;
import com.medidropbox.dto.response.LabReportResponse;
import com.medidropbox.dto.response.LatestVitalsResponse;
import com.medidropbox.dto.response.PatientResponse;
import com.medidropbox.dto.response.VitalsRecordResponse;
import com.medidropbox.enums.ReportType;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HealthProfileService;
import com.medidropbox.service.HospitalPatientService;
import com.medidropbox.service.LabReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/patients")
@Tag(name = "Hospital Patient Management", description = "Hospital-scoped patient management APIs")
public class HospitalPatientController {

    private final HospitalPatientService hospitalPatientService;
    private final LabReportService labReportService;
    private final HealthProfileService healthProfileService;

    public HospitalPatientController(HospitalPatientService hospitalPatientService, LabReportService labReportService, HealthProfileService healthProfileService) {
        this.hospitalPatientService = hospitalPatientService;
        this.labReportService = labReportService;
        this.healthProfileService = healthProfileService;
    }
    
    @PostMapping
    @Operation(summary = "Create hospital patient", description = "Create a new patient record in the hospital with full data (address, Aadhar, ABHA, emergency contacts). If global patient exists, links to it. Otherwise creates UNCLAIMED global patient.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> createHospitalPatient(
            @PathVariable Long hospitalId,
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only create for their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only create patients for your own hospital");
            }
        }
        
        // Create patient with full data from request
        return ResponseEntity.ok(hospitalPatientService.createHospitalPatientWithFullData(hospitalId, request));
    }
    
    @GetMapping
    @Operation(summary = "Get all hospital patients", description = "Get all patients for the hospital (HOSPITAL_ADMIN can only view their own hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PatientResponse>> getHospitalPatients(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view patients for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalPatientService.getHospitalPatients(hospitalId));
    }
    
    @GetMapping("/{hospitalPatientId}")
    @Operation(summary = "Get hospital patient by ID", description = "Get hospital patient details (HOSPITAL_ADMIN can only view their own hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> getHospitalPatientById(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view patients for your own hospital");
            }
        }
        
        return ResponseEntity.ok(hospitalPatientService.getHospitalPatientByIdAsResponse(hospitalId, hospitalPatientId));
    }
    
    @GetMapping("/phone/{phone}")
    @Operation(summary = "Get hospital patient by phone", description = "Get hospital patient by phone number (HOSPITAL_ADMIN can only view their own hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> getHospitalPatientByPhone(
            @PathVariable Long hospitalId,
            @PathVariable String phone,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view patients for your own hospital");
            }
        }
        
        return ResponseEntity.ok(hospitalPatientService.getHospitalPatientByPhoneAsResponse(hospitalId, phone));
    }
    
    @PutMapping("/{hospitalPatientId}")
    @Operation(summary = "Update hospital patient", description = "Update hospital patient record (HOSPITAL_ADMIN can only update their own hospital patients)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PatientResponse> updateHospitalPatient(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @Valid @RequestBody PatientRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only update their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only update patients for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalPatientService.updateHospitalPatient(hospitalId, hospitalPatientId, request));
    }
    
    @DeleteMapping("/{hospitalPatientId}")
    @Operation(summary = "Deactivate hospital patient", description = "Deactivate a hospital patient (HOSPITAL_ADMIN can only deactivate their own hospital patients)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateHospitalPatient(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only deactivate their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only deactivate patients for your own hospital");
            }
        }
        hospitalPatientService.deactivateHospitalPatient(hospitalId, hospitalPatientId);
        return ResponseEntity.ok().build();
    }

    private void ensureHospitalAccess(Long hospitalId, MediDropBoxUserDetails userDetails) {
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN || userDetails.getRole() == Role.HOSPITAL_STAFF) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only access patients and reports for your own hospital");
            }
        }
    }

    @GetMapping("/{hospitalPatientId}/reports")
    @Operation(summary = "List reports for patient", description = "If hospital has approved permission: all patient reports. Otherwise: only reports uploaded by this hospital.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<LabReportResponse>> getPatientReports(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        return ResponseEntity.ok(labReportService.getPatientReportsForHospitalContext(globalPatientId, hospitalId));
    }

    @PostMapping(value = "/{hospitalPatientId}/reports/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload report for patient", description = "Hospital staff upload a report for a patient. Report is linked to this hospital.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LabReportResponse> uploadPatientReport(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("reportType") String reportType,
            @RequestPart("reportDate") String reportDate,
            @RequestPart(value = "doctorName", required = false) String doctorName,
            @RequestPart(value = "labName", required = false) String labName,
            @RequestPart(value = "aiSummary", required = false) String aiSummary,
            @RequestPart(value = "notes", required = false) String notes,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        LabReportUploadRequest request = new LabReportUploadRequest();
        request.setFile(file);
        request.setReportType(ReportType.valueOf(reportType));
        request.setReportDate(LocalDate.parse(reportDate));
        request.setDoctorName(doctorName);
        request.setLabName(labName);
        request.setAiSummary(aiSummary);
        request.setNotes(notes);
        return ResponseEntity.ok(labReportService.uploadReportForPatientByHospital(globalPatientId, hospitalId, request));
    }

    // ---------- Health profile (same flow as reports: hospital can add; fetch own unless patient approved) ----------

    @GetMapping("/{hospitalPatientId}/health-profile")
    @Operation(summary = "Get patient health profile", description = "If hospital has HEALTH permission: full profile. Otherwise: only profile uploaded by this hospital.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HealthProfileResponse> getPatientHealthProfile(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        return ResponseEntity.ok(healthProfileService.getHealthProfileForHospitalContext(globalPatientId, hospitalId));
    }

    @PutMapping("/{hospitalPatientId}/health-profile")
    @Operation(summary = "Create or update patient health profile", description = "Hospital/doctor adds or updates health profile for patient. Stored as uploaded by this hospital.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HealthProfileResponse> createOrUpdatePatientHealthProfile(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @Valid @RequestBody HealthProfileRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        return ResponseEntity.ok(healthProfileService.createOrUpdateHealthProfileForHospital(globalPatientId, hospitalId, null, request));
    }

    @GetMapping("/{hospitalPatientId}/vitals/latest")
    @Operation(summary = "Get patient latest vitals", description = "If hospital has VITALS permission: all data. Otherwise: only vitals uploaded by this hospital.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LatestVitalsResponse> getPatientLatestVitals(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        return ResponseEntity.ok(healthProfileService.getLatestVitalsForHospitalContext(globalPatientId, hospitalId));
    }

    @PostMapping("/{hospitalPatientId}/vitals")
    @Operation(summary = "Add vitals record for patient", description = "Hospital/doctor adds a vitals record. Stored as uploaded by this hospital.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<VitalsRecordResponse> createPatientVitalsRecord(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @Valid @RequestBody VitalsRecordRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        return ResponseEntity.ok(healthProfileService.createVitalsRecordForHospital(globalPatientId, hospitalId, null, request));
    }

    @PostMapping("/{hospitalPatientId}/vitals/history")
    @Operation(summary = "Get patient vitals history", description = "If hospital has VITALS permission: all records. Otherwise: only records uploaded by this hospital. Optional body: startDate, endDate, vitalType.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<VitalsRecordResponse>> getPatientVitalsHistory(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @RequestBody(required = false) VitalsHistoryRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        VitalsHistoryRequest body = request != null ? request : new VitalsHistoryRequest();
        return ResponseEntity.ok(healthProfileService.getVitalsHistoryForHospitalContext(globalPatientId, hospitalId, body));
    }

    @GetMapping("/{hospitalPatientId}/bmi-report")
    @Operation(summary = "Get patient BMI report", description = "If hospital has HEALTH or VITALS permission: full BMI report. Otherwise: from hospital-uploaded health + vitals only.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BmiReportResponse> getPatientBmiReport(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        return ResponseEntity.ok(healthProfileService.getBmiReportForHospitalContext(globalPatientId, hospitalId));
    }
}
