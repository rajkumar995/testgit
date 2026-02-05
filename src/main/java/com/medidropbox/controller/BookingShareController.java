package com.medidropbox.controller;

import com.medidropbox.dto.request.BookingShareRequest;
import com.medidropbox.dto.response.BookingShareResponse;
import com.medidropbox.dto.response.ShareUrlResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.BookingShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking-shares")
@Tag(name = "Booking Share", description = "Booking sharing APIs")
public class BookingShareController {
    
    private final BookingShareService bookingShareService;
    
    public BookingShareController(BookingShareService bookingShareService) {
        this.bookingShareService = bookingShareService;
    }
    
    @PostMapping
    @Operation(summary = "Share booking", description = "Share a booking with friends/family (PATIENT only). Returns only the share URL.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ShareUrlResponse> shareBooking(
            @Valid @RequestBody BookingShareRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(bookingShareService.shareBooking(request, globalPatientId));
    }
    
    @GetMapping("/token/{shareToken}")
    @Operation(summary = "Get shared booking by token", description = "Get booking details using share token (Public, legacy)")
    public ResponseEntity<BookingShareResponse> getSharedBooking(@PathVariable String shareToken) {
        return ResponseEntity.ok(bookingShareService.getSharedBooking(shareToken));
    }

    @GetMapping("/view/{shortCode}")
    @Operation(summary = "Get shared booking by short code", description = "Get booking details using short code from URL (Public)")
    public ResponseEntity<BookingShareResponse> getSharedBookingByShortCode(@PathVariable String shortCode) {
        return ResponseEntity.ok(bookingShareService.getSharedBookingByShortCode(shortCode));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Revoke share", description = "Revoke a booking share (PATIENT only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> revokeShare(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        bookingShareService.revokeShare(id, globalPatientId);
        return ResponseEntity.ok().build();
    }
}
