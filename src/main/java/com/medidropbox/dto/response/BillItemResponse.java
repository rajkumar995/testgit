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
public class BillItemResponse {

    private Long id;
    private Long productId;
    private Long labTestId;
    private String itemLabel;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
