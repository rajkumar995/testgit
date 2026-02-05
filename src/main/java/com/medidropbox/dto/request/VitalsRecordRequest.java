package com.medidropbox.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalsRecordRequest {
    
    // At least one vital value is required
    @Min(value = 10, message = "Weight must be at least 10 kg")
    @Max(value = 500, message = "Weight must not exceed 500 kg")
    private Double weightKg;
    
    @Min(value = 20, message = "Blood glucose must be at least 20 mg/dL")
    @Max(value = 600, message = "Blood glucose must not exceed 600 mg/dL")
    private Double bloodGlucoseMgdl;
    
    @Min(value = 50, message = "Systolic BP must be at least 50")
    @Max(value = 250, message = "Systolic BP must not exceed 250")
    private Integer bloodPressureSystolic;
    
    @Min(value = 30, message = "Diastolic BP must be at least 30")
    @Max(value = 150, message = "Diastolic BP must not exceed 150")
    private Integer bloodPressureDiastolic;
    
    private LocalDateTime recordedAt; // Optional - defaults to now if not provided
    
    private String notes;
    
    /**
     * Validate that at least one vital value is provided
     */
    public boolean hasAtLeastOneVital() {
        return weightKg != null || bloodGlucoseMgdl != null || 
               (bloodPressureSystolic != null && bloodPressureDiastolic != null);
    }
}

