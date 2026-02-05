package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hospital Patient entity - Hospital-specific patient record
 * Each hospital maintains its own patient records
 * Links to GlobalPatient but hospital cannot see global profile
 * Full data (address, Aadhar, ABHA, emergency contacts) only saved with patient consent
 */
@Entity
@Table(name = "md_hospital_patients", uniqueConstraints = {
    @UniqueConstraint(name = "UK_hospital_patient_hospital_phone", 
        columnNames = {"hospital_id", "phone"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalPatient extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Hospital-specific patient ID

    @NotNull(message = "Hospital is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    /**
     * Link to global patient (for cross-hospital queries)
     * Hospital cannot see global profile, only uses this for linking
     */
    @NotNull(message = "Global patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id", nullable = false)
    private GlobalPatient globalPatient;

    @Column(name = "full_name")
    private String fullName; // Hospital's record of patient name

    @Column(nullable = false)
    private String phone; // Phone number (lookup key)

    @Column
    private String email; // Hospital's record of patient email

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth; // For book-for-friend / hospital records

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    /**
     * Hospital-specific notes or additional info
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Aadhar (Aadhaar) ID - Only saved if patient gives consent
     */
    @Column(name = "aadhar_id", length = 12)
    private String aadharId;
    
    /**
     * ABHA ID (Ayushman Bharat Health Account ID) - Only saved if patient gives consent
     */
    @Column(name = "abha_id")
    private String abhaId;
    
    /**
     * Patient address - Only saved if patient gives consent
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
    
    /**
     * Emergency contacts - Only saved if patient gives consent
     */
    @OneToMany(mappedBy = "hospitalPatient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<HospitalEmergencyContact> emergencyContacts = new ArrayList<>();
    
    /**
     * Data consent flag - Indicates if patient has given consent to share full data
     */
    @Column(name = "data_consent", nullable = false)
    private Boolean dataConsent = false;
}
