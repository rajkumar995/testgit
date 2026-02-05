package com.medidropbox.service;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.request.CreateHospitalRoleRequest;
import com.medidropbox.dto.response.AssignableRoleResponse;
import com.medidropbox.dto.response.HospitalRoleResponse;
import com.medidropbox.enums.Permission;

import java.util.List;

/**
 * Service for hospital admins to create and manage custom roles
 */
public interface HospitalCustomRoleService {
    HospitalRoleResponse createHospitalRole(Long hospitalId, CreateHospitalRoleRequest request);
    HospitalRoleResponse updateHospitalRole(Long hospitalId, Long roleId, CreateHospitalRoleRequest request);
    void deleteHospitalRole(Long hospitalId, Long roleId);
    List<HospitalRoleResponse> getAllHospitalRoles(Long hospitalId);
    HospitalRoleResponse getHospitalRoleById(Long hospitalId, Long roleId);
    HospitalRoleResponse assignPermissionsToHospitalRole(Long hospitalId, Long roleId, AssignPermissionRequest request);
    HospitalRoleResponse addPermissionToHospitalRole(Long hospitalId, Long roleId, Permission permission);
    HospitalRoleResponse removePermissionFromHospitalRole(Long hospitalId, Long roleId, Permission permission);
    List<AssignableRoleResponse> getAssignableRoles(Long hospitalId);
}

