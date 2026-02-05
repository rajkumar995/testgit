package com.medidropbox.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorFilterRequest {
    private Long hospitalId; // Optional - if not provided, return all doctors
    private String search; // Search on doctor name
    private String specialty;
    private BigDecimal minRating;
    private BigDecimal maxRating;
    private Boolean isActive;
    private BigDecimal minFees;
    private BigDecimal maxFees;
    private Boolean allowRemote;
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
}

