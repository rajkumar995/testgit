package com.medidropbox.controller;

import com.medidropbox.dto.request.CounterCloseRequest;
import com.medidropbox.dto.request.CounterOpenRequest;
import com.medidropbox.dto.response.PharmacyCounterResponse;
import com.medidropbox.exception.ForbiddenException;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.PharmacyCounterService;
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
@RequestMapping("/api/v1/hospitals/{hospitalId}/pharmacy/counters")
@Tag(name = "Pharmacy Counter", description = "Pharmacy counter management - open/close sessions")
@RequiredArgsConstructor
public class PharmacyCounterController {

    private final PharmacyCounterService counterService;

    private static void ensureHospitalAccess(Long hospitalId, MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
            throw new ForbiddenException("Access denied to this hospital");
        }
    }

    @PostMapping("/open")
    @Operation(summary = "Open counter", description = "Start a new counter session")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<PharmacyCounterResponse> openCounter(
            @PathVariable Long hospitalId,
            @Valid @RequestBody CounterOpenRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        String openedBy = userDetails.getUsername();
        return ResponseEntity.ok(counterService.openCounter(hospitalId, request, openedBy));
    }

    @PostMapping("/{counterId}/close")
    @Operation(summary = "Close counter", description = "Close a counter session and record closing amount")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<PharmacyCounterResponse> closeCounter(
            @PathVariable Long hospitalId,
            @PathVariable Long counterId,
            @Valid @RequestBody CounterCloseRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        String closedBy = userDetails.getUsername();
        return ResponseEntity.ok(counterService.closeCounter(hospitalId, counterId, request, closedBy));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open counter", description = "Get currently open counter for hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<PharmacyCounterResponse> getOpenCounter(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        PharmacyCounterResponse counter = counterService.getOpenCounter(hospitalId);
        if (counter == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(counter);
    }

    @GetMapping("/{counterId}")
    @Operation(summary = "Get counter by ID")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<PharmacyCounterResponse> getCounterById(
            @PathVariable Long hospitalId,
            @PathVariable Long counterId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(counterService.getCounterById(hospitalId, counterId));
    }

    @GetMapping
    @Operation(summary = "Get all counters", description = "Get all counter sessions for hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF')")
    public ResponseEntity<List<PharmacyCounterResponse>> getAllCounters(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        ensureHospitalAccess(hospitalId, userDetails);
        return ResponseEntity.ok(counterService.getAllCounters(hospitalId));
    }
}
