package com.medidropbox.controller;

import com.medidropbox.dto.request.LabTestRequest;
import com.medidropbox.dto.response.LabTestResponse;
import com.medidropbox.exception.ForbiddenException;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.LabTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/lab-tests")
@Tag(name = "Lab Tests & Charges", description = "Hospital lab test definitions and charges")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    private static void ensureHospitalAccess(Long hospitalId, MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
            throw new ForbiddenException("Access denied to this hospital");
        }
    }

    @PostMapping
    @Operation(summary = "Create lab test")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LabTestResponse> create(
            @PathVariable Long hospitalId,
            @Valid @RequestBody LabTestRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(labTestService.create(hospitalId, request));
    }

    @PutMapping("/{labTestId}")
    @Operation(summary = "Update lab test")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LabTestResponse> update(
            @PathVariable Long hospitalId,
            @PathVariable Long labTestId,
            @Valid @RequestBody LabTestRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(labTestService.update(hospitalId, labTestId, request));
    }

    @GetMapping("/{labTestId}")
    @Operation(summary = "Get lab test by ID")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LabTestResponse> getById(
            @PathVariable Long hospitalId,
            @PathVariable Long labTestId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(labTestService.getById(hospitalId, labTestId));
    }

    @GetMapping
    @Operation(summary = "List lab tests")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<LabTestResponse>> list(
            @PathVariable Long hospitalId,
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(labTestService.listByHospital(hospitalId, activeOnly));
    }

    @DeleteMapping("/{labTestId}")
    @Operation(summary = "Deactivate lab test")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long hospitalId,
            @PathVariable Long labTestId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        labTestService.delete(hospitalId, labTestId);
        return ResponseEntity.noContent().build();
    }
}
