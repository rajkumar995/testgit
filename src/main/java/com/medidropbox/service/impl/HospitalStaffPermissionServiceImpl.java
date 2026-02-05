package com.medidropbox.service.impl;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.response.RolePermissionResponse;
import com.medidropbox.entity.RolePermission;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.RolePermissionRepository;
import com.medidropbox.service.HospitalStaffPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for hospital-scoped staff permission management
 * HOSPITAL_ADMIN can manage permissions for staff roles (HOSPITAL_STAFF, DOCTOR)
 */
@Service
@Transactional
public class HospitalStaffPermissionServiceImpl implements HospitalStaffPermissionService {
    
    private final RolePermissionRepository rolePermissionRepository;
    
    // Roles that hospital admins can manage permissions for
    private static final List<Role> MANAGEABLE_STAFF_ROLES = Arrays.asList(
        Role.HOSPITAL_STAFF, 
        Role.DOCTOR
    );
    
    public HospitalStaffPermissionServiceImpl(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }
    
    @Override
    public RolePermissionResponse assignPermissionsToStaffRole(Long hospitalId, Role role, AssignPermissionRequest request) {
        validateStaffRole(role);
        
        // Remove all existing permissions for this role
        List<RolePermission> existing = rolePermissionRepository.findByRole(role);
        rolePermissionRepository.deleteAll(existing);
        
        // Add new permissions
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            List<RolePermission> newPermissions = request.getPermissions().stream()
                    .map(permission -> {
                        RolePermission rp = new RolePermission();
                        rp.setRole(role);
                        rp.setPermission(permission);
                        return rp;
                    })
                    .collect(Collectors.toList());
            rolePermissionRepository.saveAll(newPermissions);
        }
        
        return getStaffRolePermissions(hospitalId, role);
    }
    
    @Override
    public RolePermissionResponse addPermissionToStaffRole(Long hospitalId, Role role, Permission permission) {
        validateStaffRole(role);
        
        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
        return getStaffRolePermissions(hospitalId, role);
    }
    
    @Override
    public RolePermissionResponse removePermissionFromStaffRole(Long hospitalId, Role role, Permission permission) {
        validateStaffRole(role);
        
        rolePermissionRepository.findByRole(role).stream()
                .filter(rp -> rp.getPermission() == permission)
                .forEach(rolePermissionRepository::delete);
        return getStaffRolePermissions(hospitalId, role);
    }
    
    @Override
    public List<RolePermissionResponse> getStaffRolesWithPermissions(Long hospitalId) {
        return MANAGEABLE_STAFF_ROLES.stream()
                .map(role -> getStaffRolePermissions(hospitalId, role))
                .collect(Collectors.toList());
    }
    
    @Override
    public RolePermissionResponse getStaffRolePermissions(Long hospitalId, Role role) {
        validateStaffRole(role);
        
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);
        List<Permission> permissions = rolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
        
        return RolePermissionResponse.builder()
                .role(role)
                .permissions(permissions)
                .build();
    }
    
    private void validateStaffRole(Role role) {
        if (!MANAGEABLE_STAFF_ROLES.contains(role)) {
            throw new RuntimeException("HOSPITAL_ADMIN can only manage permissions for HOSPITAL_STAFF and DOCTOR roles");
        }
    }
}
