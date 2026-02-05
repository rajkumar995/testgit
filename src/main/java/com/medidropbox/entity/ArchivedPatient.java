package com.medidropbox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Archived Patient entity
 * Stores archived patient data for legal compliance and government requests
 * Data is archived instead of permanently deleted to meet legal obligations
 */
@Entity
@Table(name = "md_archived_patients", indexes = {
    @Index(name = "idx_original_global_patient_id", columnList = "original_global_patient_id"),
    @Index(name = "idx_archive_expires_at", columnList = "archive_expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArchivedPatient extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Original global patient ID (for lookup)
     */
    @Column(name = "original_global_patient_id", nullable = false)
    private Long originalGlobalPatientId;

    /**
     * Archived data in JSON format (encrypted)
     * Contains all patient data: profile, bookings, reports, permissions, etc.
     */
    @Column(name = "archived_data", columnDefinition = "LONGTEXT", nullable = false)
    private String archivedData; // Encrypted JSON

    /**
     * When data was archived (deleted from main system)
     */
    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    /**
     * Who requested the deletion (PATIENT_REQUEST, ADMIN, etc.)
     */
    @Column(name = "deleted_by", nullable = false)
    private String deletedBy;

    /**
     * Reason for deletion
     */
    @Column(name = "deletion_reason", columnDefinition = "TEXT")
    private String deletionReason;

    /**
     * Archive expiration date - Data will be permanently deleted after this
     * Set based on country's retention requirements (6-10 years)
     */
    @Column(name = "archive_expires_at", nullable = false)
    private LocalDateTime archiveExpiresAt;

    /**
     * Legal basis for archiving (HIPAA_RETENTION, GDPR_LEGAL_OBLIGATION, etc.)
     */
    @Column(name = "legal_basis", nullable = false)
    private String legalBasis;

    /**
     * Country code for retention period calculation
     */
    @Column(name = "country_code", length = 3)
    private String countryCode;

    /**
     * Whether archive has been permanently deleted
     */
    @Column(name = "permanently_deleted", nullable = false)
    private Boolean permanentlyDeleted = false;

    /**
     * When archive was permanently deleted
     */
    @Column(name = "permanently_deleted_at")
    private LocalDateTime permanentlyDeletedAt;
}
