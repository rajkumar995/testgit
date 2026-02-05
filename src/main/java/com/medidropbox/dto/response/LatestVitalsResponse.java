package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LatestVitalsResponse {
    private Double latestWeight;
    private LocalDateTime latestWeightDate;
    private Double latestBloodGlucose;
    private LocalDateTime latestBloodGlucoseDate;
    private Integer latestBloodPressureSystolic;
    private Integer latestBloodPressureDiastolic;
    private String latestBloodPressureDisplay;
    private LocalDateTime latestBloodPressureDate;
    private Double bmi;
    private String bmiCategory;
    private String bmiQuote;
    private String disclaimer;
}

