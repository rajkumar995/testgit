package com.medidropbox.dto.request;

import com.medidropbox.enums.StockMovementType;
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
public class StockMovementRequest {

    @NotNull
    private StockMovementType movementType;

    @NotNull
    private Integer quantity;

    private BigDecimal unitPrice;
    private Long referenceId;
    private String notes;
}
