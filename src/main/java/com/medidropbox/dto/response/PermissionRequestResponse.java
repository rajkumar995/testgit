package com.medidropbox.dto.response;

import com.medidropbox.enums.PermissionRequestScope;
import com.medidropbox.enums.PermissionRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestResponse {

    private Long id;
    private Long globalPatientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long hospitalId;
    private String hospitalName;
    private PermissionRequestScope scope;
    private PermissionRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;
    private String notes;
}
