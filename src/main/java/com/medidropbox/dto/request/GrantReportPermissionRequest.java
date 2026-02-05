package com.medidropbox.dto.request;

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
public class GrantReportPermissionRequest {
    
    /**
     * Doctor ID (optional - if provided, grants access to specific doctor)
     */
    private Long doctorId;
    
    /**
     * Hospital ID (optional - if provided, grants access to all doctors in that hospital)
     * If both doctorId and hospitalId are provided, grants access to specific doctor in that hospital
     */
    private Long hospitalId;
    
    /**
     * Expires at (optional - if null, permission never expires)
     */
    private LocalDateTime expiresAt;
    
    /**
     * Notes (optional - reason for granting permission)
     */
    private String notes;
}
