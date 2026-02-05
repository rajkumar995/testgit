package com.medidropbox.controller;

import com.medidropbox.dto.response.PermissionDefinitionResponse;
import com.medidropbox.enums.Permission;
import com.medidropbox.service.PermissionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permission Management", description = "Permission management APIs")
public class PermissionManagementController {
    
    private final PermissionManagementService permissionManagementService;
    
    public PermissionManagementController(PermissionManagementService permissionManagementService) {
        this.permissionManagementService = permissionManagementService;
    }
    
    @GetMapping
    @Operation(summary = "Get all permissions", description = "Get all available permissions in the system (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PermissionDefinitionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionManagementService.getAllPermissions());
    }

    @GetMapping("/{permission}")
    @Operation(summary = "Get permission details", description = "Get details of a specific permission (HOSPITAL_ADMIN can view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'PRODUCT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PermissionDefinitionResponse> getPermission(@PathVariable Permission permission) {
        return ResponseEntity.ok(permissionManagementService.getPermission(permission));
    }
}
