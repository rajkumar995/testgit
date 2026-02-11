package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Product entity for hospital inventory.
 * Each product is scoped to a hospital. Stock is derived from ProductStockMovement.
 */
@Entity
@Table(name = "md_products", indexes = {
    @Index(name = "idx_product_hospital", columnList = "hospital_id"),
    @Index(name = "idx_product_sku", columnList = "hospital_id, sku", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank(message = "Product name is required")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "sku", length = 64)
    private String sku;

    @Column(name = "unit", length = 32)
    private String unit;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    @Column(name = "sale_price", precision = 19, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
