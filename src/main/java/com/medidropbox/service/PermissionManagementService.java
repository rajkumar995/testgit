package com.medidropbox.service;

import com.medidropbox.dto.response.PermissionDefinitionResponse;
import com.medidropbox.enums.Permission;

import java.util.List;

public interface PermissionManagementService {
    List<PermissionDefinitionResponse> getAllPermissions();
    PermissionDefinitionResponse getPermission(Permission permission);
}
