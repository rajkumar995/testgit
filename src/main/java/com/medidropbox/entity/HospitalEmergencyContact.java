package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hospital Emergency Contact entity - Emergency contacts for hospital patients
 * Only saved if patient gives consent
 */
@Entity
@Table(name = "md_hospital_emergency_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalEmergencyContact extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Hospital patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_patient_id", nullable = false)
    private HospitalPatient hospitalPatient;

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
