package com.medidropbox.dto.response;

import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Role-based doctor response
 * Filters sensitive data (phone, email) based on user role
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {
    private Long id;
    private String name;
    private String title;
    private String about;
    private String specialty;
    private List<String> services;
    private String phone; // Filtered for PUBLIC
    private String email; // Filtered for PUBLIC
    private AddressResponse address;
    private List<String> expertise;
    private Long servedPatientCount;
    private BigDecimal rating;
    private String profilePhotoUrl;
    private Boolean isActive;
    private BigDecimal fees;
    private Integer averageConsultationTime;
    private Boolean allowRemote;
    private List<String> language;
    private Long hospitalId;
    private String hospitalName;
    private List<AwardResponse> awards;
    
    // Role-based filtering helper
    public static DoctorResponse filterByRole(DoctorResponse doctor, Role role) {
        if (role == null || role == Role.PUBLIC) {
            // Public: Hide sensitive contact info
            doctor.setPhone(null);
            doctor.setEmail(null);
        }
        // ADMIN, PRODUCT_ADMIN, HOSPITAL_STAFF, DOCTOR see all
        return doctor;
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
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AwardResponse {
        private Long id;
        private String awardName;
        private String awardedBy;
        private Integer awardYear;
        private String awardImageUrl;
    }
}
