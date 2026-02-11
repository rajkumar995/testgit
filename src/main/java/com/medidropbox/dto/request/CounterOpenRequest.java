package com.medidropbox.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounterOpenRequest {
    
    @NotNull(message = "Opening amount is required")
    private BigDecimal openingAmount;
    
    private String counterNumber;
    
    private String notes;
}
