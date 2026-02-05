package com.medidropbox.entity;

import com.medidropbox.enums.BloodGroup;
import com.medidropbox.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Health Profile entity - Fixed patient health details
 * One profile per patient (one-to-one relationship with GlobalPatient)
 * Updating replaces old values
 */
@Entity
@Table(name = "md_health_profiles", uniqueConstraints = {
    @UniqueConstraint(name = "UK_health_profile_patient", columnNames = {"global_patient_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfile extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Global patient is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id", nullable = false, unique = true)
    private GlobalPatient globalPatient;

    @Column(name = "height_cm")
    private Double heightCm; // Height in centimeters

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 20)
    private BloodGroup bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** Who uploaded/updated this profile (null = patient). Hospital/doctor can only fetch if they have permission or this matches their id. */
    @Column(name = "uploaded_by_hospital_id")
    private Long uploadedByHospitalId;

    @Column(name = "uploaded_by_doctor_id")
    private Long uploadedByDoctorId;
}

