package com.medidropbox.dto.request;

import com.medidropbox.enums.VitalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalsHistoryRequest {
    
    private LocalDate startDate; // Optional - filter from date
    
    private LocalDate endDate; // Optional - filter to date
    
    private VitalType vitalType = VitalType.ALL; // Optional - filter by vital type
}

