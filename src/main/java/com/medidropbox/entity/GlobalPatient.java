package com.medidropbox.entity;

import com.medidropbox.enums.PatientStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Global Patient entity - System-level patient identity (private, not visible to hospitals)
 * This is the true patient identity that links across all hospitals
 */
@Entity
@Table(name = "md_global_patients", uniqueConstraints = {
    @UniqueConstraint(name = "UK_global_patient_phone", columnNames = {"phone"}),
    @UniqueConstraint(name = "UK_global_patient_email", columnNames = {"email"}),
    @UniqueConstraint(name = "UK_global_patient_aadhar", columnNames = {"aadhar_id"}),
    @UniqueConstraint(name = "UK_global_patient_abha", columnNames = {"abha_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GlobalPatient extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Column(nullable = false, unique = true)
    private String phone; // Lookup key, not identity

    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * Patient status: UNCLAIMED (created by hospital) or CLAIMED (patient registered)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PatientStatus status = PatientStatus.UNCLAIMED;
    
    /**
     * Aadhar (Aadhaar) ID - 12-digit unique identification number
     */
    @Column(name = "aadhar_id", length = 12, unique = true)
    private String aadharId;
    
    /**
     * ABHA ID (Ayushman Bharat Health Account ID) - Health ID
     */
    @Column(name = "abha_id", unique = true)
    private String abhaId;
    
    /**
     * Patient address
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
    
    /**
     * Emergency contacts list
     */
    @OneToMany(mappedBy = "globalPatient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<EmergencyContact> emergencyContacts = new ArrayList<>();
    
    /**
     * Legal Hold - Prevents data deletion if active
     * Required for compliance (HIPAA, GDPR, DPDPA)
     */
    @Column(name = "legal_hold", nullable = false)
    private Boolean legalHold = false;
    
    /**
     * Legal hold reason
     */
    @Column(name = "legal_hold_reason", columnDefinition = "TEXT")
    private String legalHoldReason;
    
    /**
     * Legal hold expiration date
     */
    @Column(name = "legal_hold_expires_at")
    private java.time.LocalDateTime legalHoldExpiresAt;
    
    /**
     * Who placed the legal hold
     */
    @Column(name = "legal_hold_placed_by")
    private String legalHoldPlacedBy;
    
    /**
     * When legal hold was placed
     */
    @Column(name = "legal_hold_placed_at")
    private java.time.LocalDateTime legalHoldPlacedAt;
    
    // Password removed - using OTP-based authentication instead
}
