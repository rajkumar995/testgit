package com.medidropbox.service.impl;

import com.medidropbox.dto.response.PermissionDefinitionResponse;
import com.medidropbox.enums.Permission;
import com.medidropbox.service.PermissionManagementService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionManagementServiceImpl implements PermissionManagementService {

    @Override
    public List<PermissionDefinitionResponse> getAllPermissions() {
        return Arrays.stream(Permission.values())
                .map(PermissionDefinitionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDefinitionResponse getPermission(Permission permission) {
        return PermissionDefinitionResponse.from(permission);
    }
}
