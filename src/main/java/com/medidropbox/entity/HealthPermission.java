package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Health Permission entity - same flow as ReportPermission.
 * When patient approves HEALTH request, hospital/doctor can view all health profile data (patient + hospital uploaded).
 */
@Entity
@Table(name = "md_health_permissions", uniqueConstraints = {
    @UniqueConstraint(
        name = "UK_health_permission",
        columnNames = {"global_patient_id", "doctor_id", "hospital_id"}
    )
}, indexes = {
    @Index(name = "idx_hp_global_patient_id", columnList = "global_patient_id"),
    @Index(name = "idx_hp_doctor_id", columnList = "doctor_id"),
    @Index(name = "idx_hp_hospital_id", columnList = "hospital_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthPermission extends BaseAuditEntity {

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
