package com.medidropbox.dto.response;

import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Role-based hospital response
 * Filters sensitive data based on user role
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalResponse {
    private Long id;
    private String name;
    private String founder;
    private LocalDate foundedOn;
    private List<String> images;
    private AddressResponse address;
    private List<SocialMediaLinkResponse> socialMediaLinks;
    private Boolean emergencyAvailable;
    private String country;
    private List<String> services;
    private List<String> facilities;
    private String emergencyCallNumber;
    private String bookingCallNumber;
    private Boolean isActive;
    private String adminUsername; // Username created for hospital admin (only in registration response)
    
    // Role-based filtering helper
    public static HospitalResponse filterByRole(HospitalResponse hospital, Role role) {
        if (role == null || role == Role.PUBLIC) {
            // Public: Hide sensitive info
            hospital.setEmergencyCallNumber(null);
            hospital.setBookingCallNumber(null);
            hospital.setFounder(null);
        }
        // ADMIN, PRODUCT_ADMIN, HOSPITAL_STAFF see all
        return hospital;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressResponse {
        private Long id;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String country;
        private String pincode;
        private Double latitude;
        private Double longitude;
        private String locationUrl;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialMediaLinkResponse {
        private Long id;
        private String platformName;
        private String profileUrl;
    }
}
