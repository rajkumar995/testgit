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
public class ProductResponse {

    private Long id;
    private Long hospitalId;
    private String name;
    private String sku;
    private String unit;
    private String description;
    private Integer reorderLevel;
    private BigDecimal salePrice;
    private Boolean isActive;
    private Integer currentStock;
}
