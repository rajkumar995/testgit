package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportPermissionResponse {
    
    private Long id;
    private Long patientId; // Global patient ID
    private Long doctorId;
    private String doctorName; // Populated from Doctor entity
    private Long hospitalId;
    private String hospitalName; // Populated from Hospital entity
    private Boolean isActive;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private String notes;
}
