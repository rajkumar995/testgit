package com.medidropbox.controller;

import com.medidropbox.dto.request.QueueStatusUpdateRequest;
import com.medidropbox.dto.response.LiveQueueResponse;
import com.medidropbox.dto.response.QueueResponse;
import com.medidropbox.dto.response.ResolvePendingResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.PendingBookingResolutionService;
import com.medidropbox.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/queues")
@Tag(name = "Queue Management", description = "Queue management APIs")
public class QueueController {

    private final QueueService queueService;
    private final PendingBookingResolutionService pendingBookingResolutionService;

    public QueueController(QueueService queueService,
                           PendingBookingResolutionService pendingBookingResolutionService) {
        this.queueService = queueService;
        this.pendingBookingResolutionService = pendingBookingResolutionService;
    }
    
    @GetMapping("/live/doctor/{doctorId}")
    @Operation(summary = "Get live queue", description = "Get live queue for a doctor on a date (staff/doctor only; patients use GET /my-queues)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<LiveQueueResponse> getLiveQueue(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        return ResponseEntity.ok(queueService.getLiveQueue(doctorId, date, userDetails.getRole(), userDetails.getGlobalPatientId()));
    }

    @GetMapping("/full/doctor/{doctorId}")
    @Operation(summary = "Get full queue", description = "Get full queue details (HOSPITAL_ADMIN/STAFF/DOCTOR only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<QueueResponse>> getFullQueue(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        return ResponseEntity.ok(queueService.getFullQueue(doctorId, date, userDetails.getRole(), userDetails.getGlobalPatientId()));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update queue status", description = "Update queue status (HOSPITAL_ADMIN/STAFF/ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<QueueResponse> updateQueueStatus(
            @PathVariable Long id,
            @Valid @RequestBody QueueStatusUpdateRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        return ResponseEntity.ok(queueService.updateQueueStatus(id, request, userDetails.getRole()));
    }
    
    @GetMapping("/my-queues")
    @Operation(summary = "Get my queues", description = "Get ALL queues for the current patient across ALL hospitals (cross-hospital query)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<QueueResponse>> getMyQueues(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(queueService.getMyQueues(globalPatientId, userDetails.getRole()));
    }

    /**
     * Resolve pending bookings for all dates before today (or before optional upToDate).
     * Applies each hospital's setting: AUTO_CANCEL, CARRY_OVER, or MANUAL (skip).
     * ADMIN/SUPER_ADMIN: all hospitals. HOSPITAL_ADMIN: only their hospital.
     */
    @PostMapping("/resolve-pending-bookings")
    @Operation(summary = "Resolve pending bookings (past days)", description = "Process pending queues (WAITING/CALLED) with bookingDate before today. Applies hospital setting: AUTO_CANCEL, CARRY_OVER, or MANUAL. ADMIN/SUPER_ADMIN: all hospitals; HOSPITAL_ADMIN: own hospital only. Optional upToDate: process all dates before this date.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ResolvePendingResponse> resolvePendingBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate upToDate,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long hospitalId = (userDetails.getRole() == Role.HOSPITAL_ADMIN) ? userDetails.getHospitalId() : null;
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN && hospitalId == null) {
            throw new RuntimeException("Hospital admin must be linked to a hospital");
        }
        ResolvePendingResponse result = upToDate != null
                ? pendingBookingResolutionService.resolvePendingBookingsBefore(upToDate, hospitalId)
                : pendingBookingResolutionService.resolvePendingBookingsForPastDays(hospitalId);
        return ResponseEntity.ok(result);
    }
}
