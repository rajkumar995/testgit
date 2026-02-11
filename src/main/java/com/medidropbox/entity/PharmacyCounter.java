package com.medidropbox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pharmacy Counter entity for tracking counter sessions
 * Each counter session tracks opening/closing times and amounts
 */
@Entity
@Table(name = "md_pharmacy_counters", indexes = {
    @Index(name = "idx_counter_hospital", columnList = "hospital_id"),
    @Index(name = "idx_counter_status", columnList = "hospital_id, is_open")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PharmacyCounter extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "counter_number", length = 50)
    private String counterNumber;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "opening_amount", precision = 19, scale = 2)
    private BigDecimal openingAmount;

    @Column(name = "closing_amount", precision = 19, scale = 2)
    private BigDecimal closingAmount;

    @Column(name = "total_sales", precision = 19, scale = 2)
    private BigDecimal totalSales;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = true;

    @Column(name = "opened_by", length = 255)
    private String openedBy;

    @Column(name = "closed_by", length = 255)
    private String closedBy;

    @Column(name = "notes", length = 1000)
    private String notes;
}
