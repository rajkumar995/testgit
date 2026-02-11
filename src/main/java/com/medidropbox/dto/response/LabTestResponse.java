package com.medidropbox.dto.response;

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
public class LabTestResponse {

    private Long id;
    private Long hospitalId;
    private String name;
    private String code;
    private BigDecimal charge;
    private String description;
    private Boolean isActive;
}
