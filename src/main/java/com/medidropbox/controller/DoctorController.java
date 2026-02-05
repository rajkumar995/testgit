package com.medidropbox.controller;

import com.medidropbox.dto.request.DoctorFilterRequest;
import com.medidropbox.dto.response.DoctorResponse;
import com.medidropbox.dto.response.PagedResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.DoctorService;
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
@RequestMapping("/api/v1/doctors")
@Tag(name = "Doctor Management", description = "Doctor management APIs")
public class DoctorController {
    
    private final DoctorService doctorService;
    
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Get doctor details with role-based filtering")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DoctorResponse> getDoctorById(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Role role = userDetails != null ? userDetails.getRole() : Role.PUBLIC;
        return ResponseEntity.ok(doctorService.getDoctorById(id, role));
    }
    
    @GetMapping("/{id}/public")
    @Operation(summary = "Get doctor by ID (Public)", description = "Public endpoint to get doctor details")
    public ResponseEntity<DoctorResponse> getDoctorByIdPublic(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id, Role.PUBLIC));
    }
    
    @GetMapping("/hospital/{hospitalId}")
    @Operation(summary = "Get doctors by hospital", description = "Get all doctors for a hospital with role-based filtering")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByHospital(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Role role = userDetails != null ? userDetails.getRole() : Role.PUBLIC;
        return ResponseEntity.ok(doctorService.getDoctorsByHospital(hospitalId, role));
    }
    
    @GetMapping("/hospital/{hospitalId}/public")
    @Operation(summary = "Get doctors by hospital (Public)", description = "Public endpoint to get doctors for a hospital")
    public ResponseEntity<List<DoctorResponse>> getDoctorsByHospitalPublic(@PathVariable Long hospitalId) {
        return ResponseEntity.ok(doctorService.getDoctorsByHospital(hospitalId, Role.PUBLIC));
    }
    
    @GetMapping
    @Operation(summary = "Search doctors with filters", description = "Search doctors with filters (name, specialty, ratings, isActive, fees range, allowRemote, hospitalId) and pagination. If hospitalId is not provided, returns all doctors. Protected endpoint.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PagedResponse<DoctorResponse>> getAllDoctors(
            @ModelAttribute DoctorFilterRequest filterRequest,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Role role = userDetails != null ? userDetails.getRole() : Role.PUBLIC;
        return ResponseEntity.ok(doctorService.searchDoctors(
                filterRequest != null ? filterRequest : new DoctorFilterRequest(), 
                role));
    }
    
    @GetMapping("/public")
    @Operation(summary = "Search doctors with filters (Public)", description = "Public endpoint to search doctors with filters (name, specialty, ratings, isActive, fees range, allowRemote, hospitalId) and pagination. If hospitalId is not provided, returns all doctors.")
    public ResponseEntity<PagedResponse<DoctorResponse>> getAllDoctorsPublic(
            @ModelAttribute DoctorFilterRequest filterRequest) {
        return ResponseEntity.ok(doctorService.searchDoctors(
                filterRequest != null ? filterRequest : new DoctorFilterRequest(), 
                Role.PUBLIC));
    }
    
    @PostMapping
    @Operation(summary = "Create doctor", description = "Create a new doctor. HOSPITAL_ADMIN can only create for their own hospital (hospitalId is auto-set)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(
            @Valid @RequestBody DoctorResponse request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        
        // For HOSPITAL_ADMIN and HOSPITAL_STAFF, automatically set hospitalId to their hospital
        if ((userDetails.getRole() == Role.HOSPITAL_ADMIN || userDetails.getRole() == Role.HOSPITAL_STAFF) 
                && userHospitalId != null) {
            request.setHospitalId(userHospitalId);
        }
        
        return ResponseEntity.ok(doctorService.createDoctor(request, userDetails.getRole(), userHospitalId));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update doctor", description = "Update doctor details. Doctor can update self. HOSPITAL_ADMIN can update doctors from their hospital. ADMIN can update any.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('DOCTOR', 'HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorResponse request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        return ResponseEntity.ok(doctorService.updateDoctor(id, request, userDetails.getRole(), userDetails.getUserId(), userHospitalId));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (deactivate) doctor", description = "Deactivate a doctor. HOSPITAL_ADMIN can only delete doctors from their own hospital. ADMIN can delete any.")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteDoctor(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Long userHospitalId = userDetails.getHospitalId();
        doctorService.deleteDoctor(id, userDetails.getRole(), userHospitalId);
        return ResponseEntity.ok().build();
    }
}
