package com.medidropbox.dto.response;

import com.medidropbox.enums.PermissionRequestScope;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Unified permission response for all scopes (REPORTS, VITALS, HEALTH)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    
    private Long id;
    private Long patientId; // Global patient ID
    private PermissionRequestScope scope; // REPORTS, VITALS, or HEALTH
    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;
    private Boolean isActive;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private String notes;
}
