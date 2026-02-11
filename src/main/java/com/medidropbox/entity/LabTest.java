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
 * Lab test definition with charge, scoped per hospital.
 */
@Entity
@Table(name = "md_lab_tests", indexes = {
    @Index(name = "idx_lab_test_hospital", columnList = "hospital_id"),
    @Index(name = "idx_lab_test_code", columnList = "hospital_id, code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabTest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank(message = "Lab test name is required")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", length = 64)
    private String code;

    @NotNull
    @Column(name = "charge", nullable = false, precision = 19, scale = 2)
    private BigDecimal charge;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
