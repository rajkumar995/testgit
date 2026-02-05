package com.medidropbox.dto.request;

import com.medidropbox.enums.Permission;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignPermissionRequest {
    @NotNull(message = "Permissions list is required")
    private List<Permission> permissions;
}
