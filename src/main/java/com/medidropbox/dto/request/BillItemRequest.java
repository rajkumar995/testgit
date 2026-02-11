package com.medidropbox.dto.request;

import jakarta.validation.constraints.Min;
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
public class BillItemRequest {

    private Long productId;
    private Long labTestId;

    @NotNull
    @Min(1)
    private Integer quantity = 1;

    private BigDecimal unitPrice;

    /** If null, backend uses product sale price or lab test charge */
    public BigDecimal getUnitPriceOrNull() {
        return unitPrice;
    }
}
