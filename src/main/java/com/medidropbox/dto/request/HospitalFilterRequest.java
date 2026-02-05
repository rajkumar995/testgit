package com.medidropbox.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalFilterRequest {
    private String state;
    private String city;
    private String pincode;
    private Boolean emergencyAvailable;
    private Boolean isActive;
    private Double latitude;
    private Double longitude;
    private Double radius; // in kilometers for location-based search
    private Boolean is24x7; // Check if "24/7" or "24*7" is in facilities
    private Boolean hasAmbulance; // Check if "ambulance" is in facilities or services
    private String search; // Search on hospital name
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}

