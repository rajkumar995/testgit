package com.medidropbox.controller;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.response.RolePermissionResponse;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import com.medidropbox.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Role Management", description = "Role and permission management APIs")
public class RoleManagementController {
    
    private final RoleManagementService roleManagementService;
    
    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }
    
    @GetMapping
    @Operation(summary = "Get all roles with permissions", description = "Get all roles with their assigned permissions")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RolePermissionResponse>> getAllRolesWithPermissions() {
        return ResponseEntity.ok(roleManagementService.getAllRolesWithPermissions());
    }
    
    @GetMapping("/{role}")
    @Operation(summary = "Get permissions for a role", description = "Get all permissions assigned to a specific role")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> getRolePermissions(@PathVariable Role role) {
        return ResponseEntity.ok(roleManagementService.getRolePermissions(role));
    }
    
    @PutMapping("/{role}/permissions")
    @Operation(summary = "Assign permissions to role", description = "Replace all permissions for a role with new permissions (ADMIN/PRODUCT_ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> assignPermissionsToRole(
            @PathVariable Role role,
            @Valid @RequestBody AssignPermissionRequest request) {
        return ResponseEntity.ok(roleManagementService.assignPermissionsToRole(role, request));
    }
    
    @PostMapping("/{role}/permissions/{permission}")
    @Operation(summary = "Add permission to role", description = "Add a single permission to a role (ADMIN/PRODUCT_ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> addPermissionToRole(
            @PathVariable Role role,
            @PathVariable Permission permission) {
        return ResponseEntity.ok(roleManagementService.addPermissionToRole(role, permission));
    }
    
    @DeleteMapping("/{role}/permissions/{permission}")
    @Operation(summary = "Remove permission from role", description = "Remove a permission from a role (ADMIN/PRODUCT_ADMIN only)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RolePermissionResponse> removePermissionFromRole(
            @PathVariable Role role,
            @PathVariable Permission permission) {
        return ResponseEntity.ok(roleManagementService.removePermissionFromRole(role, permission));
    }
    
    @GetMapping("/{role}/permissions")
    @Operation(summary = "Get permissions list for role", description = "Get list of permissions for a role")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<Permission>> getPermissionsForRole(@PathVariable Role role) {
        return ResponseEntity.ok(roleManagementService.getPermissionsForRole(role));
    }
}
