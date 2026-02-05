package com.medidropbox.dto.request;

import com.medidropbox.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffRequest {
    @NotBlank(message = "Username is required")
    private String username; // Email or phone
    
    @NotBlank(message = "Password is required")
    private String password;
    
    @NotNull(message = "Role is required")
    private Role role; // HOSPITAL_STAFF, DOCTOR, etc.
    
    private String name; // Optional name for the staff
}
