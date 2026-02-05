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
public class VitalsRecordResponse {
    private Long id;
    private Long globalPatientId;
    private Double weightKg;
    private Double bloodGlucoseMgdl;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private String bloodPressureDisplay; // e.g., "120/80"
    private LocalDateTime recordedAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

