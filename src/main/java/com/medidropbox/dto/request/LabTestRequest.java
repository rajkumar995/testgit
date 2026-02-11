package com.medidropbox.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class LabTestRequest {

    @NotBlank(message = "Lab test name is required")
    private String name;

    private String code;

    @NotNull(message = "Charge is required")
    private BigDecimal charge;

    private String description;
    private Boolean isActive = true;
}
