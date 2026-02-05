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
public class BmiReportResponse {
    private Double bmi;
    private String bmiCategory;
    private String bmiDescription;
    private String inspirationalQuote;
    private Double heightCm;
    private Double weightKg;
    private LocalDateTime weightRecordedAt;
    private String healthRecommendation;
    private String disclaimer;
}

