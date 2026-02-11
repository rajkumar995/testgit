package com.medidropbox.controller;

import com.medidropbox.dto.request.BillRequest;
import com.medidropbox.dto.response.BillResponse;
import com.medidropbox.exception.ForbiddenException;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/bills")
@Tag(name = "Billing / Cart", description = "Hospital bills for customers (cart and complete)")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    private static void ensureHospitalAccess(Long hospitalId, MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
            throw new ForbiddenException("Access denied to this hospital");
        }
    }

    @PostMapping
    @Operation(summary = "Create bill (draft cart)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BillResponse> create(
            @PathVariable Long hospitalId,
            @Valid @RequestBody BillRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(billService.create(hospitalId, request));
    }

    @PutMapping("/{billId}")
    @Operation(summary = "Update bill (draft only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BillResponse> update(
            @PathVariable Long hospitalId,
            @PathVariable Long billId,
            @Valid @RequestBody BillRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(billService.update(hospitalId, billId, request));
    }

    @GetMapping("/{billId}")
    @Operation(summary = "Get bill by ID")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BillResponse> getById(
            @PathVariable Long hospitalId,
            @PathVariable Long billId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(billService.getById(hospitalId, billId));
    }

    @PostMapping("/{billId}/complete")
    @Operation(summary = "Complete bill (deduct stock, finalize)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BillResponse> complete(
            @PathVariable Long hospitalId,
            @PathVariable Long billId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(billService.completeBill(hospitalId, billId));
    }

    @GetMapping
    @Operation(summary = "List bills (paginated)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<BillResponse>> list(
            @PathVariable Long hospitalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(billService.listByHospital(hospitalId, PageRequest.of(page, size)));
    }

    @GetMapping("/by-patient/{hospitalPatientId}")
    @Operation(summary = "List bills for a patient")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BillResponse>> listByPatient(
            @PathVariable Long hospitalId,
            @PathVariable Long hospitalPatientId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(billService.listByHospitalAndPatient(hospitalId, hospitalPatientId));
    }
}
