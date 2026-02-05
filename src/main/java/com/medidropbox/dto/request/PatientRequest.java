package com.medidropbox.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Phone is required")
    private String phone;
    
    @Email(message = "Email must be valid")
    private String email;
    
    private String profileImageUrl;
    
    /**
     * Aadhar (Aadhaar) ID - 12-digit unique identification number
     */
    @Pattern(regexp = "^\\d{12}$", message = "Aadhar ID must be exactly 12 digits")
    private String aadharId;
    
    /**
     * ABHA ID (Ayushman Bharat Health Account ID) - Health ID
     */
    private String abhaId;
    
    /**
     * Patient address
     */
    @Valid
    private AddressRequest address;
    
    /**
     * Emergency contacts list
     */
    @Valid
    private List<EmergencyContactRequest> emergencyContacts;
}
