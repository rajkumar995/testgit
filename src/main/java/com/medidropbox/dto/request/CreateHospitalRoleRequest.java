package com.medidropbox.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateHospitalRoleRequest {
    @NotBlank(message = "Role name is required")
    private String roleName;
    
    private String description;
}

