package com.medidropbox.controller;

import com.medidropbox.dto.request.BookingRequest;
import com.medidropbox.dto.response.BookingResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.BookingService;
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
@RequestMapping("/api/v1/bookings")
@Tag(name = "Booking Management", description = "Booking management APIs")
public class BookingController {
    
    private final BookingService bookingService;
    
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    
    @PostMapping
    @Operation(summary = "Create booking (Patient)", description = "Create a new booking (PATIENT only). Queue is automatically created. Uses global patient ID from authenticated user. To book for another person (friend): send bookForName, bookForPhone, bookForDateOfBirth (optional), bookForEmail (optional), bookForDescription (optional), and bookOnBehalfConsent: true. Only Hospital Patient is created for friend; booking owner remains you; friend can access via Share.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Get global patient ID from authenticated user
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(bookingService.createBooking(request, globalPatientId, userDetails.getUserId()));
    }
    
    @PostMapping("/hospital/{hospitalId}")
    @Operation(summary = "Create booking (Hospital Staff)", description = "Create a new booking for a patient (HOSPITAL_ADMIN/STAFF only). Requires phoneNumber. Creates/get hospital patient if needed.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<BookingResponse> createBookingByHospital(
            @PathVariable Long hospitalId,
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only create for their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only create bookings for your own hospital");
            }
        }
        
        // Verify hospitalId matches
        if (!hospitalId.equals(request.getHospitalId())) {
            throw new RuntimeException("Hospital ID mismatch");
        }
        
        // Hospital staff booking - no globalPatientId (will be created/linked via phone)
        return ResponseEntity.ok(bookingService.createBooking(request, null, userDetails.getUserId()));
    }
    
    @GetMapping("/my-bookings")
    @Operation(summary = "Get my bookings", description = "Get ALL bookings for the current patient across ALL hospitals (cross-hospital query)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Get global patient ID to fetch all bookings across all hospitals
        Long globalPatientId = userDetails.getGlobalPatientId();
        if (globalPatientId == null) {
            throw new RuntimeException("Patient not linked to user account");
        }
        return ResponseEntity.ok(bookingService.getMyBookings(globalPatientId, userDetails.getRole()));
    }
    
    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get bookings by hospital", description = "Get all bookings for a hospital (HOSPITAL_ADMIN/STAFF/ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByHospital(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getBookingsByHospital(hospitalId, userDetails.getRole()));
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get bookings by doctor", description = "Get all bookings for a doctor (HOSPITAL_ADMIN/STAFF/ADMIN/DOCTOR only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'DOCTOR', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByDoctor(
            @PathVariable Long doctorId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getBookingsByDoctor(doctorId, userDetails.getRole()));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Get booking details with role-based filtering")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long globalPatientId = userDetails != null ? userDetails.getGlobalPatientId() : null;
        return ResponseEntity.ok(bookingService.getBookingById(id, 
                userDetails != null ? userDetails.getRole() : Role.PUBLIC, 
                userDetails != null ? userDetails.getUserId() : null, 
                globalPatientId));
    }
}
