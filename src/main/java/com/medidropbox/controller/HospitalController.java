package com.medidropbox.controller;

import com.medidropbox.dto.request.HospitalFilterRequest;
import com.medidropbox.dto.request.HospitalRegistrationRequest;
import com.medidropbox.dto.response.HospitalResponse;
import com.medidropbox.dto.response.PagedResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HospitalService;
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
@RequestMapping("/api/v1/hospitals")
@Tag(name = "Hospital Management", description = "Hospital management APIs")
public class HospitalController {
    
    private final HospitalService hospitalService;
    
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }
    
    @PostMapping
    @Operation(summary = "Register hospital", description = "Public endpoint to register a new hospital")
    public ResponseEntity<HospitalResponse> registerHospital(@Valid @RequestBody HospitalRegistrationRequest request) {
        return ResponseEntity.ok(hospitalService.registerHospital(request));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get hospital by ID", description = "Get hospital details with role-based filtering")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<HospitalResponse> getHospitalById(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Role role = userDetails != null ? userDetails.getRole() : Role.PUBLIC;
        return ResponseEntity.ok(hospitalService.getHospitalById(id, role));
    }
    
    @GetMapping("/{id}/public")
    @Operation(summary = "Get hospital by ID (Public)", description = "Public endpoint to get hospital details")
    public ResponseEntity<HospitalResponse> getHospitalByIdPublic(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id, Role.PUBLIC));
    }
    
    @GetMapping
    @Operation(summary = "Get all hospitals or search with filters", description = "Get all active hospitals or search with filters (state, city, pincode, emergency, active, location, 24x7, ambulance, name search) and pagination. If page/size params are provided, returns paginated results. Protected endpoint.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAllHospitals(
            @ModelAttribute HospitalFilterRequest filterRequest,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        Role role = userDetails != null ? userDetails.getRole() : Role.PUBLIC;
        
        // If pagination params are provided or any filter is set, use search
        boolean hasFilters = filterRequest != null && (
                filterRequest.getPage() != null || 
                filterRequest.getSize() != null ||
                filterRequest.getState() != null ||
                filterRequest.getCity() != null ||
                filterRequest.getPincode() != null ||
                filterRequest.getEmergencyAvailable() != null ||
                filterRequest.getIsActive() != null ||
                filterRequest.getLatitude() != null ||
                filterRequest.getLongitude() != null ||
                filterRequest.getIs24x7() != null ||
                filterRequest.getHasAmbulance() != null ||
                (filterRequest.getSearch() != null && !filterRequest.getSearch().trim().isEmpty())
        );
        
        if (hasFilters) {
            return ResponseEntity.ok(hospitalService.searchHospitals(
                    filterRequest != null ? filterRequest : new HospitalFilterRequest(), 
                    role));
        } else {
            return ResponseEntity.ok(hospitalService.getAllHospitals(role));
        }
    }
    
    @GetMapping("/public")
    @Operation(summary = "Get all hospitals or search with filters (Public)", description = "Public endpoint to get all active hospitals or search with filters (state, city, pincode, emergency, active, location, 24x7, ambulance, name search) and pagination. If page/size params are provided, returns paginated results.")
    public ResponseEntity<?> getAllHospitalsPublic(
            @ModelAttribute HospitalFilterRequest filterRequest) {
        // If pagination params are provided or any filter is set, use search
        boolean hasFilters = filterRequest != null && (
                filterRequest.getPage() != null || 
                filterRequest.getSize() != null ||
                filterRequest.getState() != null ||
                filterRequest.getCity() != null ||
                filterRequest.getPincode() != null ||
                filterRequest.getEmergencyAvailable() != null ||
                filterRequest.getIsActive() != null ||
                filterRequest.getLatitude() != null ||
                filterRequest.getLongitude() != null ||
                filterRequest.getIs24x7() != null ||
                filterRequest.getHasAmbulance() != null ||
                (filterRequest.getSearch() != null && !filterRequest.getSearch().trim().isEmpty())
        );
        
        if (hasFilters) {
            return ResponseEntity.ok(hospitalService.searchHospitals(
                    filterRequest != null ? filterRequest : new HospitalFilterRequest(), 
                    Role.PUBLIC));
        } else {
            return ResponseEntity.ok(hospitalService.getAllHospitals(Role.PUBLIC));
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update hospital", description = "Update hospital details. HOSPITAL_ADMIN can only update their own hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalResponse> updateHospital(
            @PathVariable Long id,
            @Valid @RequestBody HospitalRegistrationRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only update their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(id)) {
                throw new RuntimeException("You can only update your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalService.updateHospital(id, request, userDetails.getRole()));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate hospital", description = "Deactivate hospital (ADMIN/PRODUCT_ADMIN only, HOSPITAL_ADMIN cannot deactivate)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateHospital(
            @PathVariable Long id,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        hospitalService.deactivateHospital(id, userDetails.getRole());
        return ResponseEntity.ok().build();
    }
}
