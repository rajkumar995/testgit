package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Report Permission entity
 * Allows patients to grant access to specific doctors/hospitals to view their reports
 */
@Entity
@Table(name = "md_report_permissions", uniqueConstraints = {
    @UniqueConstraint(
        name = "UK_report_permission",
        columnNames = {"global_patient_id", "doctor_id", "hospital_id"}
    )
}, indexes = {
    @Index(name = "idx_global_patient_id", columnList = "global_patient_id"),
    @Index(name = "idx_doctor_id", columnList = "doctor_id"),
    @Index(name = "idx_hospital_id", columnList = "hospital_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportPermission extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Global Patient ID - Patient who is granting permission
     */
    @NotNull(message = "Patient ID is required")
    @Column(name = "global_patient_id", nullable = false)
    private Long globalPatientId;

    /**
     * Doctor ID - Doctor who is granted access (nullable if hospital-level access)
     */
    @Column(name = "doctor_id")
    private Long doctorId;

    /**
     * Hospital ID - Hospital whose doctors can access (nullable if doctor-specific)
     */
    @Column(name = "hospital_id")
    private Long hospitalId;

    /**
     * Is Active - Can be revoked by patient
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Permission granted at
     */
    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt = LocalDateTime.now();

    /**
     * Expires at (optional, null means never expires)
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /**
     * Notes (why permission was granted)
     */
    @Column(name = "notes", length = 500)
    private String notes;
}
