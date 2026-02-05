package com.medidropbox.controller;

import com.medidropbox.dto.request.CreatePermissionRequestRequest;
import com.medidropbox.dto.response.PermissionRequestResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HospitalPatientService;
import com.medidropbox.service.PermissionRequestService;
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
@RequestMapping("/api/v1/hospitals/{hospitalId}/permission-requests")
@Tag(name = "Hospital Permission Requests", description = "Doctor/hospital request access to patient reports/vitals/health")
public class HospitalPermissionRequestController {

    private final PermissionRequestService permissionRequestService;
    private final HospitalPatientService hospitalPatientService;

    public HospitalPermissionRequestController(
            PermissionRequestService permissionRequestService,
            HospitalPatientService hospitalPatientService) {
        this.permissionRequestService = permissionRequestService;
        this.hospitalPatientService = hospitalPatientService;
    }

    @PostMapping
    @Operation(summary = "Create permission request", description = "Request access to patient's reports/vitals/health. Patient must approve.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PermissionRequestResponse> create(
            @PathVariable Long hospitalId,
            @Valid @RequestBody CreatePermissionRequestRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        if (userDetails.getHospitalId() == null || !userDetails.getHospitalId().equals(hospitalId)) {
            throw new RuntimeException("You can only create requests for your own hospital");
        }
        PermissionRequestResponse created = permissionRequestService.create(hospitalId, null, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "List my permission requests", description = "All requests made by this hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PermissionRequestResponse>> listMyRequests(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        if (userDetails.getHospitalId() == null || !userDetails.getHospitalId().equals(hospitalId)) {
            throw new RuntimeException("You can only view your own hospital's requests");
        }
        List<PermissionRequestResponse> list = permissionRequestService.listMyRequests(null, hospitalId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/patient/{hospitalPatientId}")
    @Operation(summary = "List permission requests for patient", description = "Requests made by this hospital for this patient")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PermissionRequestResponse>> listForPatient(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        if (userDetails.getHospitalId() == null || !userDetails.getHospitalId().equals(hospitalId)) {
            throw new RuntimeException("You can only view requests for your own hospital");
        }
        Long globalPatientId = hospitalPatientService.getGlobalPatientId(hospitalId, hospitalPatientId);
        List<PermissionRequestResponse> list = permissionRequestService.listForPatient(globalPatientId, null, hospitalId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{requestId}/cancel")
    @Operation(summary = "Cancel permission request", description = "Cancel a pending request (hospital/doctor only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PermissionRequestResponse> cancel(
            @PathVariable Long hospitalId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        if (userDetails.getHospitalId() == null || !userDetails.getHospitalId().equals(hospitalId)) {
            throw new RuntimeException("You can only cancel your own hospital's requests");
        }
        PermissionRequestResponse updated = permissionRequestService.cancel(requestId, hospitalId, null);
        return ResponseEntity.ok(updated);
    }
}
