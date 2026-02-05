package com.medidropbox.entity;

import com.medidropbox.enums.PermissionRequestScope;
import com.medidropbox.enums.PermissionRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Permission request: doctor/hospital requests access to patient's reports/vitals/health.
 * Patient can approve or reject. When approved, a ReportPermission is created (for reports scope).
 */
@Entity
@Table(name = "md_permission_requests", indexes = {
    @Index(name = "idx_pr_patient", columnList = "global_patient_id"),
    @Index(name = "idx_pr_doctor", columnList = "doctor_id"),
    @Index(name = "idx_pr_hospital", columnList = "hospital_id"),
    @Index(name = "idx_pr_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "global_patient_id", nullable = false)
    private Long globalPatientId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private PermissionRequestScope scope;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PermissionRequestStatus status = PermissionRequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "notes", length = 500)
    private String notes;
}
