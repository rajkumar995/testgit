package com.medidropbox.service.impl;

import com.medidropbox.dto.request.AssignPermissionRequest;
import com.medidropbox.dto.request.CreateHospitalRoleRequest;
import com.medidropbox.dto.response.AssignableRoleResponse;
import com.medidropbox.dto.response.HospitalRoleResponse;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.HospitalRole;
import com.medidropbox.entity.HospitalRolePermission;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.HospitalRolePermissionRepository;
import com.medidropbox.repository.HospitalRoleRepository;
import com.medidropbox.service.HospitalCustomRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for hospital custom role management
 * HOSPITAL_ADMIN can create, update, delete custom roles and assign permissions
 */
@Service
@Transactional
public class HospitalCustomRoleServiceImpl implements HospitalCustomRoleService {
    
    private final HospitalRoleRepository hospitalRoleRepository;
    private final HospitalRolePermissionRepository hospitalRolePermissionRepository;
    private final HospitalRepository hospitalRepository;
    
    public HospitalCustomRoleServiceImpl(
            HospitalRoleRepository hospitalRoleRepository,
            HospitalRolePermissionRepository hospitalRolePermissionRepository,
            HospitalRepository hospitalRepository) {
        this.hospitalRoleRepository = hospitalRoleRepository;
        this.hospitalRolePermissionRepository = hospitalRolePermissionRepository;
        this.hospitalRepository = hospitalRepository;
    }
    
    @Override
    public HospitalRoleResponse createHospitalRole(Long hospitalId, CreateHospitalRoleRequest request) {
        // Verify hospital exists
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found with id: " + hospitalId));
        
        // Check if role name already exists for this hospital
        if (hospitalRoleRepository.existsByHospitalIdAndRoleName(hospitalId, request.getRoleName())) {
            throw new RuntimeException("Role with name '" + request.getRoleName() + "' already exists for this hospital");
        }
        
        // Create new hospital role
        HospitalRole hospitalRole = new HospitalRole();
        hospitalRole.setHospital(hospital);
        hospitalRole.setRoleName(request.getRoleName());
        hospitalRole.setDescription(request.getDescription());
        hospitalRole.setIsActive(true);
        
        hospitalRole = hospitalRoleRepository.save(hospitalRole);
        
        return mapToResponse(hospitalRole);
    }
    
    @Override
    public HospitalRoleResponse updateHospitalRole(Long hospitalId, Long roleId, CreateHospitalRoleRequest request) {
        HospitalRole hospitalRole = hospitalRoleRepository.findByIdAndHospitalId(roleId, hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital role not found with id: " + roleId + " for hospital: " + hospitalId));
        
        // Check if new role name conflicts with existing role (excluding current role)
        if (!hospitalRole.getRoleName().equals(request.getRoleName())) {
            if (hospitalRoleRepository.existsByHospitalIdAndRoleName(hospitalId, request.getRoleName())) {
                throw new RuntimeException("Role with name '" + request.getRoleName() + "' already exists for this hospital");
            }
        }
        
        hospitalRole.setRoleName(request.getRoleName());
        hospitalRole.setDescription(request.getDescription());
        
        hospitalRole = hospitalRoleRepository.save(hospitalRole);
        
        return mapToResponse(hospitalRole);
    }
    
    @Override
    public void deleteHospitalRole(Long hospitalId, Long roleId) {
        HospitalRole hospitalRole = hospitalRoleRepository.findByIdAndHospitalId(roleId, hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital role not found with id: " + roleId + " for hospital: " + hospitalId));
        
        // Delete all permissions first
        hospitalRolePermissionRepository.deleteByHospitalRoleId(roleId);
        
        // Delete the role
        hospitalRoleRepository.delete(hospitalRole);
    }
    
    @Override
    public List<HospitalRoleResponse> getAllHospitalRoles(Long hospitalId) {
        List<HospitalRole> roles = hospitalRoleRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
        return roles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public HospitalRoleResponse getHospitalRoleById(Long hospitalId, Long roleId) {
        HospitalRole hospitalRole = hospitalRoleRepository.findByIdAndHospitalId(roleId, hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital role not found with id: " + roleId + " for hospital: " + hospitalId));
        
        return mapToResponse(hospitalRole);
    }
    
    @Override
    public HospitalRoleResponse assignPermissionsToHospitalRole(Long hospitalId, Long roleId, AssignPermissionRequest request) {
        HospitalRole hospitalRole = hospitalRoleRepository.findByIdAndHospitalId(roleId, hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital role not found with id: " + roleId + " for hospital: " + hospitalId));
        
        // Remove all existing permissions for this role
        hospitalRolePermissionRepository.deleteByHospitalRoleId(roleId);
        
        // Add new permissions
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            List<HospitalRolePermission> newPermissions = request.getPermissions().stream()
                    .map(permission -> {
                        HospitalRolePermission hrp = new HospitalRolePermission();
                        hrp.setHospitalRole(hospitalRole);
                        hrp.setPermission(permission);
                        return hrp;
                    })
                    .collect(Collectors.toList());
            hospitalRolePermissionRepository.saveAll(newPermissions);
        }
        
        return getHospitalRoleById(hospitalId, roleId);
    }
    
    @Override
    public HospitalRoleResponse addPermissionToHospitalRole(Long hospitalId, Long roleId, Permission permission) {
        HospitalRole hospitalRole = hospitalRoleRepository.findByIdAndHospitalId(roleId, hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital role not found with id: " + roleId + " for hospital: " + hospitalId));
        
        if (!hospitalRolePermissionRepository.existsByHospitalRoleIdAndPermission(roleId, permission)) {
            HospitalRolePermission hrp = new HospitalRolePermission();
            hrp.setHospitalRole(hospitalRole);
            hrp.setPermission(permission);
            hospitalRolePermissionRepository.save(hrp);
        }
        
        return getHospitalRoleById(hospitalId, roleId);
    }
    
    @Override
    public HospitalRoleResponse removePermissionFromHospitalRole(Long hospitalId, Long roleId, Permission permission) {
        // Verify role exists
        hospitalRoleRepository.findByIdAndHospitalId(roleId, hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital role not found with id: " + roleId + " for hospital: " + hospitalId));
        
        hospitalRolePermissionRepository.deleteByHospitalRoleIdAndPermission(roleId, permission);
        
        return getHospitalRoleById(hospitalId, roleId);
    }
    
    @Override
    public List<AssignableRoleResponse> getAssignableRoles(Long hospitalId) {
        List<AssignableRoleResponse> assignableRoles = new ArrayList<>();
        
        // Add system roles that hospitals can assign to staff
        List<Role> assignableSystemRoles = Arrays.asList(Role.HOSPITAL_STAFF, Role.DOCTOR);
        for (Role role : assignableSystemRoles) {
            assignableRoles.add(AssignableRoleResponse.builder()
                    .roleId(role.name())
                    .roleName(role.name())
                    .roleType("SYSTEM")
                    .description(getSystemRoleDescription(role))
                    .build());
        }
        
        // Add custom hospital roles
        List<HospitalRole> customRoles = hospitalRoleRepository.findByHospitalIdAndIsActiveTrue(hospitalId);
        for (HospitalRole customRole : customRoles) {
            assignableRoles.add(AssignableRoleResponse.builder()
                    .roleId("CUSTOM_" + customRole.getId())
                    .roleName(customRole.getRoleName())
                    .roleType("CUSTOM")
                    .description(customRole.getDescription())
                    .customRoleId(customRole.getId())
                    .build());
        }
        
        return assignableRoles;
    }
    
    private String getSystemRoleDescription(Role role) {
        switch (role) {
            case HOSPITAL_STAFF:
                return "Regular hospital staff member";
            case DOCTOR:
                return "Doctor/Physician";
            default:
                return "";
        }
    }
    
    private HospitalRoleResponse mapToResponse(HospitalRole hospitalRole) {
        List<Permission> permissions = hospitalRolePermissionRepository.findByHospitalRoleId(hospitalRole.getId())
                .stream()
                .map(HospitalRolePermission::getPermission)
                .collect(Collectors.toList());
        
        return HospitalRoleResponse.builder()
                .id(hospitalRole.getId())
                .hospitalId(hospitalRole.getHospital().getId())
                .roleName(hospitalRole.getRoleName())
                .description(hospitalRole.getDescription())
                .isActive(hospitalRole.getIsActive())
                .permissions(permissions)
                .build();
    }
}

