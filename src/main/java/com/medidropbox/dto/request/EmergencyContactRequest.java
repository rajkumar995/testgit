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
public class EmergencyContactRequest {
    @NotBlank(message = "Person name is required")
    private String personName;
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    private String relationship; // e.g., "Father", "Mother", "Spouse", "Friend"
    
    private String email;
    
    private Boolean isPrimary = false; // Primary emergency contact
}
