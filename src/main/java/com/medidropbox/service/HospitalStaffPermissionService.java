package com.medidropbox.service;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.response.RolePermissionResponse;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;

import java.util.List;

/**
 * Service for hospital admins to manage permissions for their staff roles
 * HOSPITAL_ADMIN can modify permissions for HOSPITAL_STAFF, DOCTOR roles (for their hospital context)
 */
public interface HospitalStaffPermissionService {
    RolePermissionResponse assignPermissionsToStaffRole(Long hospitalId, Role role, AssignPermissionRequest request);
    RolePermissionResponse addPermissionToStaffRole(Long hospitalId, Role role, Permission permission);
    RolePermissionResponse removePermissionFromStaffRole(Long hospitalId, Role role, Permission permission);
    List<RolePermissionResponse> getStaffRolesWithPermissions(Long hospitalId);
    RolePermissionResponse getStaffRolePermissions(Long hospitalId, Role role);
}
