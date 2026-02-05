package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Emergency Contact entity - Emergency contacts for patients
 */
@Entity
@Table(name = "md_emergency_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContact extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Global patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id", nullable = false)
    private GlobalPatient globalPatient;

    @NotBlank(message = "Contact person name is required")
    @Column(name = "person_name", nullable = false)
    private String personName;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phone;

    @Column(name = "relationship")
    private String relationship; // e.g., "Father", "Mother", "Spouse", "Friend", etc.

    @Column(name = "email")
    private String email;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false; // Primary emergency contact

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
