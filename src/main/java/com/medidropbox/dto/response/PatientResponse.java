package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientResponse {
    private Long id;
    /** Global patient ID (for permission requests / cross-hospital linking). */
    private Long globalPatientId;
    private String fullName;
    private String phone;
    private String email;
    private String profileImageUrl;
    private String aadharId;
    private String abhaId;
    private AddressResponse address;
    private List<EmergencyContactResponse> emergencyContacts;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
