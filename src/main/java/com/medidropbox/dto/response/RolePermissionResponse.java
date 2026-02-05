package com.medidropbox.dto.response;

import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionResponse {
    private Long id;
    private Role role;
    private List<Permission> permissions;
}
