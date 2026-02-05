package com.medidropbox.controller;

import com.medidropbox.dto.request.GrantReportPermissionRequest;
import com.medidropbox.dto.response.PermissionRequestResponse;
import com.medidropbox.dto.response.PermissionResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
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
@RequestMapping("/api/v1/patients/permission-requests")
@Tag(name = "Patient Permission Requests", description = "Unified API: view requests, approve/reject/revoke, and manage all granted permissions (REPORTS, VITALS, HEALTH)")
public class PatientPermissionRequestController {

    private final PermissionRequestService permissionRequestService;

    public PatientPermissionRequestController(PermissionRequestService permissionRequestService) {
        this.permissionRequestService = permissionRequestService;
    }

    @GetMapping
    @Operation(summary = "List my permission requests", description = "All requests for your data (pending and history)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PermissionRequestResponse>> listMyRequests(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        List<PermissionRequestResponse> list = permissionRequestService.listForAuthenticatedPatient(globalPatientId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{requestId}/approve")
    @Operation(summary = "Approve permission request", description = "Grant access; creates permission (ReportPermission/VitalsPermission/HealthPermission)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PermissionRequestResponse> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        PermissionRequestResponse updated = permissionRequestService.approve(requestId, globalPatientId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{requestId}/reject")
    @Operation(summary = "Reject permission request", description = "Deny access. Doctor can request again.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PermissionRequestResponse> reject(
            @PathVariable Long requestId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        PermissionRequestResponse updated = permissionRequestService.reject(requestId, globalPatientId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{requestId}/revoke")
    @Operation(summary = "Revoke approved request", description = "Revoke an approved request (removes the granted permission for REPORTS/VITALS/HEALTH)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PermissionRequestResponse> revoke(
            @PathVariable Long requestId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        PermissionRequestResponse updated = permissionRequestService.revoke(requestId, globalPatientId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get all granted permissions", description = "Get all permissions you have granted (REPORTS, VITALS, HEALTH)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PermissionResponse>> getAllGrantedPermissions(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        List<PermissionResponse> permissions = permissionRequestService.getAllGrantedPermissions(globalPatientId);
        return ResponseEntity.ok(permissions);
    }

    @PostMapping("/permissions")
    @Operation(summary = "Grant report permission directly", description = "Grant report access directly without request flow (REPORTS scope only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PermissionResponse> grantReportPermission(
            @Valid @RequestBody GrantReportPermissionRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        PermissionResponse permission = permissionRequestService.grantReportPermission(globalPatientId, request);
        return ResponseEntity.ok(permission);
    }

    @DeleteMapping("/permissions/{permissionId}")
    @Operation(summary = "Revoke permission", description = "Revoke any granted permission by ID. Requires scope query param (REPORTS, VITALS, or HEALTH)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> revokePermission(
            @PathVariable Long permissionId,
            @RequestParam String scope,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        permissionRequestService.revokePermission(permissionId, scope, globalPatientId);
        return ResponseEntity.ok().build();
    }
}
