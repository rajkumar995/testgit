package com.medidropbox.dto.request;

import com.medidropbox.enums.PermissionRequestScope;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePermissionRequestRequest {

    @NotNull(message = "Patient ID is required")
    private Long globalPatientId;

    @NotNull(message = "Scope is required (REPORTS, VITALS, HEALTH)")
    private PermissionRequestScope scope;

    private LocalDateTime expiresAt;
    private String notes;
}
