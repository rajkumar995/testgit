package com.medidropbox.controller;

import com.medidropbox.dto.request.CreateStaffRequest;
import com.medidropbox.dto.response.AssignableRoleResponse;
import com.medidropbox.dto.response.StaffResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HospitalCustomRoleService;
import com.medidropbox.service.StaffManagementService;
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
@RequestMapping("/api/v1/hospitals/{hospitalId}/staff")
@Tag(name = "Staff Management", description = "Hospital staff management APIs (Hospital-scoped)")
public class StaffManagementController {
    
    private final StaffManagementService staffManagementService;
    private final HospitalCustomRoleService hospitalCustomRoleService;
    
    public StaffManagementController(StaffManagementService staffManagementService,
                                     HospitalCustomRoleService hospitalCustomRoleService) {
        this.staffManagementService = staffManagementService;
        this.hospitalCustomRoleService = hospitalCustomRoleService;
    }
    
    @PostMapping
    @Operation(summary = "Create staff", description = "Create a new staff member for the hospital (HOSPITAL_ADMIN can only manage their own hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StaffResponse> createStaff(
            @PathVariable Long hospitalId,
            @Valid @RequestBody CreateStaffRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage staff for your own hospital");
            }
        }
        return ResponseEntity.ok(staffManagementService.createStaff(hospitalId, request));
    }
    
    @GetMapping
    @Operation(summary = "Get all staff", description = "Get all staff members for the hospital (HOSPITAL_ADMIN can only view their own hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<StaffResponse>> getStaffByHospital(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view staff for your own hospital");
            }
        }
        return ResponseEntity.ok(staffManagementService.getStaffByHospital(hospitalId));
    }
    
    @GetMapping("/{staffId}")
    @Operation(summary = "Get staff by ID", description = "Get staff member details (HOSPITAL_ADMIN can only view their own hospital staff)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StaffResponse> getStaffById(
            @PathVariable Long hospitalId,
            @PathVariable Long staffId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view staff for your own hospital");
            }
        }
        return ResponseEntity.ok(staffManagementService.getStaffById(staffId, hospitalId));
    }
    
    @PutMapping("/{staffId}")
    @Operation(summary = "Update staff", description = "Update staff member (HOSPITAL_ADMIN can only update their own hospital staff)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StaffResponse> updateStaff(
            @PathVariable Long hospitalId,
            @PathVariable Long staffId,
            @Valid @RequestBody CreateStaffRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only update their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only update staff for your own hospital");
            }
        }
        return ResponseEntity.ok(staffManagementService.updateStaff(staffId, hospitalId, request));
    }
    
    @DeleteMapping("/{staffId}")
    @Operation(summary = "Deactivate staff", description = "Deactivate a staff member (HOSPITAL_ADMIN can only deactivate their own hospital staff)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateStaff(
            @PathVariable Long hospitalId,
            @PathVariable Long staffId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only deactivate their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only deactivate staff for your own hospital");
            }
        }
        staffManagementService.deactivateStaff(staffId, hospitalId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{staffId}/role")
    @Operation(summary = "Assign role to staff", description = "Assign/change role for a staff member (HOSPITAL_ADMIN can only manage their own hospital staff)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<StaffResponse> assignRoleToStaff(
            @PathVariable Long hospitalId,
            @PathVariable Long staffId,
            @RequestParam Role role,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage staff for your own hospital");
            }
        }
        return ResponseEntity.ok(staffManagementService.assignRoleToStaff(staffId, hospitalId, role));
    }
    
    @GetMapping("/assignable-roles")
    @Operation(summary = "Get assignable roles", description = "Get list of all roles that can be assigned to staff (system roles + custom hospital roles)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<AssignableRoleResponse>> getAssignableRoles(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.getAssignableRoles(hospitalId));
    }
}
