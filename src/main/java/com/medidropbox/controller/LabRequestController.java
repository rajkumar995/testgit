package com.medidropbox.controller;

import com.medidropbox.dto.request.LabRequestCreateRequest;
import com.medidropbox.dto.request.LabRequestUpdateRequest;
import com.medidropbox.dto.response.LabRequestResponse;
import com.medidropbox.exception.ForbiddenException;
import com.medidropbox.repository.DoctorRepository;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.LabRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lab-requests")
@Tag(name = "Lab Requests", description = "Lab test request management with barcode tracking")
@RequiredArgsConstructor
public class LabRequestController {
    
    private final LabRequestService labRequestService;
    private final DoctorRepository doctorRepository;
    
    @PostMapping
    @Operation(summary = "Create lab request", description = "Doctor or hospital creates a lab test request. Barcode is auto-generated.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> createRequest(
            @Valid @RequestBody LabRequestCreateRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        Long hospitalId = userDetails.getHospitalId();
        Long doctorId = findDoctorId(userDetails);
        
        LabRequestResponse response = labRequestService.createRequest(hospitalId, doctorId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/lab/{labId}")
    @Operation(summary = "Get all requests for a lab", description = "Get all lab requests assigned to a specific lab/hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<Page<LabRequestResponse>> getLabRequests(
            @PathVariable Long labId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        ensureHospitalAccess(labId, userDetails);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(labRequestService.getLabRequests(labId, pageable));
    }
    
    @GetMapping("/lab/{labId}/status/{status}")
    @Operation(summary = "Get requests by status for a lab", description = "Filter lab requests by status (REQUESTED, SAMPLE_RECEIVED, IN_PROGRESS, COMPLETED, etc.)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<Page<LabRequestResponse>> getLabRequestsByStatus(
            @PathVariable Long labId,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        ensureHospitalAccess(labId, userDetails);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(labRequestService.getLabRequestsByStatus(labId, status, pageable));
    }
    
    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get requests created by hospital", description = "Get all lab requests created by a specific hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<Page<LabRequestResponse>> getHospitalRequests(
            @PathVariable Long hospitalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        ensureHospitalAccess(hospitalId, userDetails);
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(labRequestService.getHospitalRequests(hospitalId, pageable));
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get requests created by doctor", description = "Get all lab requests created by a specific doctor")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<Page<LabRequestResponse>> getDoctorRequests(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        // Doctor can only see their own requests
        Long currentDoctorId = findDoctorId(userDetails);
        if (currentDoctorId == null || !currentDoctorId.equals(doctorId)) {
            throw new ForbiddenException("Access denied");
        }
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(labRequestService.getDoctorRequests(doctorId, pageable));
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get requests for a patient", description = "Get all lab requests for a specific patient")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<Page<LabRequestResponse>> getPatientRequests(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(labRequestService.getPatientRequests(patientId, pageable));
    }
    
    @GetMapping("/{requestId}")
    @Operation(summary = "Get request by ID")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> getRequestById(@PathVariable Long requestId) {
        return ResponseEntity.ok(labRequestService.getRequestById(requestId));
    }
    
    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Get request by barcode", description = "Lookup lab request using barcode (for sample tracking)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> getRequestByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(labRequestService.getRequestByBarcode(barcode));
    }
    
    @PutMapping("/{requestId}")
    @Operation(summary = "Update lab request", description = "Update request status, assign lab, or update other details")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> updateRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody LabRequestUpdateRequest request) {
        return ResponseEntity.ok(labRequestService.updateRequest(requestId, request));
    }
    
    @PutMapping("/{requestId}/sample-received")
    @Operation(summary = "Mark sample as received", description = "Update status to SAMPLE_RECEIVED when sample arrives at lab")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> markSampleReceived(
            @PathVariable Long requestId,
            @RequestParam String receivedBy) {
        return ResponseEntity.ok(labRequestService.markSampleReceived(requestId, receivedBy));
    }
    
    @PutMapping("/{requestId}/start-test")
    @Operation(summary = "Start test", description = "Update status to IN_PROGRESS when test begins")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> startTest(@PathVariable Long requestId) {
        return ResponseEntity.ok(labRequestService.startTest(requestId));
    }
    
    @PutMapping("/{requestId}/complete-test")
    @Operation(summary = "Complete test", description = "Update status to COMPLETED and link lab report")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<LabRequestResponse> completeTest(
            @PathVariable Long requestId,
            @RequestParam Long labReportId) {
        return ResponseEntity.ok(labRequestService.completeTest(requestId, labReportId));
    }
    
    @PutMapping("/{requestId}/cancel")
    @Operation(summary = "Cancel request", description = "Cancel a lab request")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String reason) {
        labRequestService.cancelRequest(requestId, reason);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{requestId}")
    @Operation(summary = "Delete request", description = "Soft delete a lab request")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long requestId) {
        labRequestService.deleteRequest(requestId);
        return ResponseEntity.noContent().build();
    }
    
    private void ensureHospitalAccess(Long hospitalId, MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
            throw new ForbiddenException("Access denied to this hospital");
        }
    }
    
    private Long findDoctorId(MediDropBoxUserDetails userDetails) {
        return doctorRepository.findByEmailAndIsActiveTrue(userDetails.getUsername())
            .map(d -> d.getId())
            .orElseGet(() -> 
                doctorRepository.findByPhoneAndIsActiveTrue(userDetails.getUsername())
                    .map(d -> d.getId())
                    .orElse(null)
            );
    }
}
