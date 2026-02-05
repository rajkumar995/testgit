package com.medidropbox.controller;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.request.CreateHospitalRoleRequest;
import com.medidropbox.dto.response.HospitalRoleResponse;
import com.medidropbox.dto.response.RolePermissionResponse;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HospitalCustomRoleService;
import com.medidropbox.service.HospitalStaffPermissionService;
import com.medidropbox.service.RoleManagementService;
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
@RequestMapping("/api/v1/hospitals/{hospitalId}/roles")
@Tag(name = "Hospital Role Management", description = "Hospital-scoped role and permission management APIs")
public class HospitalRoleManagementController {
    
    private final RoleManagementService roleManagementService;
    private final HospitalStaffPermissionService hospitalStaffPermissionService;
    private final HospitalCustomRoleService hospitalCustomRoleService;
    
    public HospitalRoleManagementController(RoleManagementService roleManagementService,
                                           HospitalStaffPermissionService hospitalStaffPermissionService,
                                           HospitalCustomRoleService hospitalCustomRoleService) {
        this.roleManagementService = roleManagementService;
        this.hospitalStaffPermissionService = hospitalStaffPermissionService;
        this.hospitalCustomRoleService = hospitalCustomRoleService;
    }
    
    @GetMapping
    @Operation(summary = "Get all roles with permissions", description = "Get all roles with their assigned permissions (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getAllRolesWithPermissions(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(roleManagementService.getAllRolesWithPermissions());
    }
    
    @GetMapping("/{role}")
    @Operation(summary = "Get permissions for a role", description = "Get all permissions assigned to a specific role (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> getRolePermissions(
            @PathVariable Long hospitalId,
            @PathVariable Role role,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(roleManagementService.getRolePermissions(role));
    }
    
    @GetMapping("/staff")
    @Operation(summary = "Get staff roles with permissions", description = "Get staff roles (HOSPITAL_STAFF, DOCTOR) with their permissions (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getStaffRolesWithPermissions(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalStaffPermissionService.getStaffRolesWithPermissions(hospitalId));
    }
    
    @PutMapping("/staff/{role}/permissions")
    @Operation(summary = "Assign permissions to staff role", description = "Assign permissions to HOSPITAL_STAFF or DOCTOR role (HOSPITAL_ADMIN can manage for their hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> assignPermissionsToStaffRole(
            @PathVariable Long hospitalId,
            @PathVariable Role role,
            @Valid @RequestBody AssignPermissionRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage permissions for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalStaffPermissionService.assignPermissionsToStaffRole(hospitalId, role, request));
    }
    
    @PostMapping("/staff/{role}/permissions/{permission}")
    @Operation(summary = "Add permission to staff role", description = "Add permission to HOSPITAL_STAFF or DOCTOR role (HOSPITAL_ADMIN can manage)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> addPermissionToStaffRole(
            @PathVariable Long hospitalId,
            @PathVariable Role role,
            @PathVariable Permission permission,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage permissions for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalStaffPermissionService.addPermissionToStaffRole(hospitalId, role, permission));
    }
    
    @DeleteMapping("/staff/{role}/permissions/{permission}")
    @Operation(summary = "Remove permission from staff role", description = "Remove permission from HOSPITAL_STAFF or DOCTOR role (HOSPITAL_ADMIN can manage)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> removePermissionFromStaffRole(
            @PathVariable Long hospitalId,
            @PathVariable Role role,
            @PathVariable Permission permission,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage permissions for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalStaffPermissionService.removePermissionFromStaffRole(hospitalId, role, permission));
    }
    
    @GetMapping("/staff/{role}")
    @Operation(summary = "Get staff role permissions", description = "Get permissions for HOSPITAL_STAFF or DOCTOR role (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> getStaffRolePermissions(
            @PathVariable Long hospitalId,
            @PathVariable Role role,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalStaffPermissionService.getStaffRolePermissions(hospitalId, role));
    }
    
    // ========== Custom Hospital Role Management Endpoints ==========
    
    @PostMapping("/custom")
    @Operation(summary = "Create custom hospital role", description = "Create a new custom role for the hospital (HOSPITAL_ADMIN can create for their hospital)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalRoleResponse> createCustomRole(
            @PathVariable Long hospitalId,
            @Valid @RequestBody CreateHospitalRoleRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only create roles for their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only create roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.createHospitalRole(hospitalId, request));
    }
    
    @GetMapping("/custom")
    @Operation(summary = "Get all custom hospital roles", description = "Get all custom roles created by the hospital (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<HospitalRoleResponse>> getAllCustomRoles(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.getAllHospitalRoles(hospitalId));
    }
    
    @GetMapping("/custom/{roleId}")
    @Operation(summary = "Get custom hospital role by ID", description = "Get a specific custom role with its permissions (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalRoleResponse> getCustomRoleById(
            @PathVariable Long hospitalId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only view their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only view roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.getHospitalRoleById(hospitalId, roleId));
    }
    
    @PutMapping("/custom/{roleId}")
    @Operation(summary = "Update custom hospital role", description = "Update a custom role's name and description (HOSPITAL_ADMIN can update)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalRoleResponse> updateCustomRole(
            @PathVariable Long hospitalId,
            @PathVariable Long roleId,
            @Valid @RequestBody CreateHospitalRoleRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only update their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only update roles for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.updateHospitalRole(hospitalId, roleId, request));
    }
    
    @DeleteMapping("/custom/{roleId}")
    @Operation(summary = "Delete custom hospital role", description = "Delete a custom role (HOSPITAL_ADMIN can delete)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCustomRole(
            @PathVariable Long hospitalId,
            @PathVariable Long roleId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only delete their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only delete roles for your own hospital");
            }
        }
        hospitalCustomRoleService.deleteHospitalRole(hospitalId, roleId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/custom/{roleId}/permissions")
    @Operation(summary = "Assign permissions to custom role", description = "Assign permissions to a custom hospital role (HOSPITAL_ADMIN can manage)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalRoleResponse> assignPermissionsToCustomRole(
            @PathVariable Long hospitalId,
            @PathVariable Long roleId,
            @Valid @RequestBody AssignPermissionRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage permissions for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.assignPermissionsToHospitalRole(hospitalId, roleId, request));
    }
    
    @PostMapping("/custom/{roleId}/permissions/{permission}")
    @Operation(summary = "Add permission to custom role", description = "Add a permission to a custom hospital role (HOSPITAL_ADMIN can manage)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalRoleResponse> addPermissionToCustomRole(
            @PathVariable Long hospitalId,
            @PathVariable Long roleId,
            @PathVariable Permission permission,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage permissions for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.addPermissionToHospitalRole(hospitalId, roleId, permission));
    }
    
    @DeleteMapping("/custom/{roleId}/permissions/{permission}")
    @Operation(summary = "Remove permission from custom role", description = "Remove a permission from a custom hospital role (HOSPITAL_ADMIN can manage)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalRoleResponse> removePermissionFromCustomRole(
            @PathVariable Long hospitalId,
            @PathVariable Long roleId,
            @PathVariable Permission permission,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only manage their own hospital
        if (userDetails.getRole() == Role.HOSPITAL_ADMIN) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only manage permissions for your own hospital");
            }
        }
        return ResponseEntity.ok(hospitalCustomRoleService.removePermissionFromHospitalRole(hospitalId, roleId, permission));
    }
}
