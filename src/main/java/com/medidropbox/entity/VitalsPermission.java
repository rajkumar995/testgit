package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Vitals Permission entity - same flow as ReportPermission.
 * When patient approves VITALS request, hospital/doctor can view all vitals data (patient + hospital uploaded).
 */
@Entity
@Table(name = "md_vitals_permissions", uniqueConstraints = {
    @UniqueConstraint(
        name = "UK_vitals_permission",
        columnNames = {"global_patient_id", "doctor_id", "hospital_id"}
    )
}, indexes = {
    @Index(name = "idx_vp_global_patient_id", columnList = "global_patient_id"),
    @Index(name = "idx_vp_doctor_id", columnList = "doctor_id"),
    @Index(name = "idx_vp_hospital_id", columnList = "hospital_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VitalsPermission extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Patient ID is required")
    @Column(name = "global_patient_id", nullable = false)
    private Long globalPatientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "notes", length = 500)
    private String notes;
}
