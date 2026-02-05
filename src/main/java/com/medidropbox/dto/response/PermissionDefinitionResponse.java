package com.medidropbox.dto.response;

import com.medidropbox.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response for listing RBAC permission definitions (e.g. MANAGE_HOSPITAL, VIEW_PATIENT).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDefinitionResponse {

    private String name;

    public static PermissionDefinitionResponse from(Permission permission) {
        return new PermissionDefinitionResponse(permission.name());
    }
}
