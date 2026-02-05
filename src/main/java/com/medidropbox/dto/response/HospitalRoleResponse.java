package com.medidropbox.dto.response;

import com.medidropbox.enums.Permission;
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
public class HospitalRoleResponse {
    private Long id;
    private Long hospitalId;
    private String roleName;
    private String description;
    private Boolean isActive;
    private List<Permission> permissions;
}

