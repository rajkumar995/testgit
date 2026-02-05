package com.medidropbox.service.impl;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.response.RolePermissionResponse;
import com.medidropbox.entity.RolePermission;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.RolePermissionRepository;
import com.medidropbox.service.RoleManagementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleManagementServiceImpl implements RoleManagementService {
    
    private final RolePermissionRepository rolePermissionRepository;
    
    public RoleManagementServiceImpl(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }
    
    @Override
    public List<RolePermissionResponse> getAllRolesWithPermissions() {
        return List.of(Role.values()).stream()
                .map(this::getRolePermissions)
                .collect(Collectors.toList());
    }
    
    @Override
    public RolePermissionResponse getRolePermissions(Role role) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(role);
        List<Permission> permissions = rolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
        
        return RolePermissionResponse.builder()
                .role(role)
                .permissions(permissions)
                .build();
    }
    
    @Override
    public RolePermissionResponse assignPermissionsToRole(Role role, AssignPermissionRequest request) {
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
        
        return getRolePermissions(role);
    }
    
    @Override
    public RolePermissionResponse addPermissionToRole(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
        return getRolePermissions(role);
    }
    
    @Override
    public RolePermissionResponse removePermissionFromRole(Role role, Permission permission) {
        rolePermissionRepository.findByRole(role).stream()
                .filter(rp -> rp.getPermission() == permission)
                .forEach(rolePermissionRepository::delete);
        return getRolePermissions(role);
    }
    
    @Override
    public List<Permission> getPermissionsForRole(Role role) {
        return rolePermissionRepository.findByRole(role).stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }
}
