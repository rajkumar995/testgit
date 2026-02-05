package com.medidropbox.service;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.response.RolePermissionResponse;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;

import java.util.List;

public interface RoleManagementService {
    List<RolePermissionResponse> getAllRolesWithPermissions();
    RolePermissionResponse getRolePermissions(Role role);
    RolePermissionResponse assignPermissionsToRole(Role role, AssignPermissionRequest request);
    RolePermissionResponse addPermissionToRole(Role role, Permission permission);
    RolePermissionResponse removePermissionFromRole(Role role, Permission permission);
    List<Permission> getPermissionsForRole(Role role);
}
