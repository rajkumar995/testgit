package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Vitals Record entity - History-based patient vitals
 * Each submission creates a new record (preserves full history)
 * Patients can update or delete their own records
 */
@Entity
@Table(name = "md_vitals_records", indexes = {
    @Index(name = "IDX_vitals_patient_created", columnList = "global_patient_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalsRecord extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Global patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id", nullable = false)
    private GlobalPatient globalPatient;

    @Column(name = "weight_kg")
    private Double weightKg; // Weight in kilograms

    @Column(name = "blood_glucose_mgdl")
    private Double bloodGlucoseMgdl; // Blood glucose in mg/dL

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic; // Systolic BP

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic; // Diastolic BP

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt; // When the vitals were recorded (can be different from created_at)

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Optional notes about this vitals entry

    /** Who uploaded this record (null = patient). Hospital can only fetch their own unless they have permission. */
    @Column(name = "uploaded_by_hospital_id")
    private Long uploadedByHospitalId;

    @Column(name = "uploaded_by_doctor_id")
    private Long uploadedByDoctorId;
}

