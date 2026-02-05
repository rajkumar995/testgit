package com.medidropbox.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRegistrationRequest {
    @NotBlank(message = "Hospital name is required")
    private String name;
    
    private String founder;
    private LocalDate foundedOn;
    private List<String> images;
    
    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;
    
    private List<SocialMediaLinkRequest> socialMediaLinks;
    private Boolean emergencyAvailable = false;
    
    @NotBlank(message = "Country is required")
    private String country;
    
    private List<String> services;
    private List<String> facilities;
    private String emergencyCallNumber;
    private String bookingCallNumber;
    
    // Admin user credentials for hospital login (only required during registration, optional for updates)
    private String adminUsername; // Email or phone for login
    
    private String adminPassword; // Will be encrypted
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;
        private String addressLine2;
        @NotBlank(message = "City is required")
        private String city;
        @NotBlank(message = "State is required")
        private String state;
        @NotBlank(message = "Country is required")
        private String country;
        @NotBlank(message = "Pincode is required")
        private String pincode;
        private Double latitude;
        private Double longitude;
        private String locationUrl;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialMediaLinkRequest {
        @NotBlank(message = "Platform name is required")
        private String platformName;
        @NotBlank(message = "Profile URL is required")
        private String profileUrl;
    }
}
