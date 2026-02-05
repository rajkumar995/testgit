package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignableRoleResponse {
    private String roleId; // For system roles: enum name, for custom roles: "custom_{id}"
    private String roleName; // Display name
    private String roleType; // "SYSTEM" or "CUSTOM"
    private String description;
    private Long customRoleId; // Only for custom roles
}

