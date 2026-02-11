package com.medidropbox.dto.response;

import com.medidropbox.enums.StockMovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private StockMovementType movementType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private Long referenceId;
    private String notes;
    private LocalDateTime createdAt;
}
