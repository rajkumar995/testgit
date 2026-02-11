package com.medidropbox.entity;

import com.medidropbox.enums.LabRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lab Request entity
 * Tracks lab test requests from doctors/hospitals, sample collection, and report generation
 */
@Entity
@Table(name = "md_lab_requests", indexes = {
    @Index(name = "idx_lab_request_hospital", columnList = "requesting_hospital_id"),
    @Index(name = "idx_lab_request_doctor", columnList = "requesting_doctor_id"),
    @Index(name = "idx_lab_request_patient", columnList = "patient_id"),
    @Index(name = "idx_lab_request_lab", columnList = "assigned_lab_id"),
    @Index(name = "idx_lab_request_status", columnList = "status"),
    @Index(name = "idx_lab_request_barcode", columnList = "barcode", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabRequest extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hospital that requested the test (null if requested by doctor)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_hospital_id")
    private Hospital requestingHospital;

    /**
     * Doctor that requested the test (null if requested by hospital)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_doctor_id")
    private Doctor requestingDoctor;

    /**
     * Patient for whom the test is requested
     */
    @NotNull(message = "Patient is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private HospitalPatient patient;

    /**
     * Lab test to be performed
     */
    @NotNull(message = "Lab test is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    /**
     * Lab/Hospital assigned to perform the test
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_lab_id")
    private Hospital assignedLab;

    /**
     * Request status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private LabRequestStatus status = LabRequestStatus.REQUESTED;

    /**
     * Unique barcode for sample tracking
     */
    @NotBlank(message = "Barcode is required")
    @Column(name = "barcode", nullable = false, unique = true, length = 100)
    private String barcode;

    /**
     * Request notes/instructions
     */
    @Column(name = "request_notes", columnDefinition = "TEXT")
    private String requestNotes;

    /**
     * Sample received date/time
     */
    @Column(name = "sample_received_at")
    private LocalDateTime sampleReceivedAt;

    /**
     * Sample received by (staff name)
     */
    @Column(name = "sample_received_by", length = 255)
    private String sampleReceivedBy;

    /**
     * Test started date/time
     */
    @Column(name = "test_started_at")
    private LocalDateTime testStartedAt;

    /**
     * Test completed date/time
     */
    @Column(name = "test_completed_at")
    private LocalDateTime testCompletedAt;

    /**
     * Generated lab report (if completed)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_report_id")
    private LabReport labReport;

    /**
     * Rejection reason (if sample rejected)
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Priority (NORMAL, URGENT, STAT)
     */
    @Column(name = "priority", length = 20)
    private String priority = "NORMAL";

    /**
     * Is Active (for soft delete)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
