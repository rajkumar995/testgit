package com.medidropbox.entity;

import com.medidropbox.enums.FileFormat;
import com.medidropbox.enums.ReportType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Lab Report / Medical Report entity
 * Stores patient medical reports with encryption and AWS S3 storage
 */
@Entity
@Table(name = "md_lab_reports", indexes = {
    @Index(name = "idx_global_patient_id", columnList = "global_patient_id"),
    @Index(name = "idx_report_type", columnList = "report_type"),
    @Index(name = "idx_report_date", columnList = "report_date"),
    @Index(name = "idx_uploaded_by_hospital_id", columnList = "uploaded_by_hospital_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabReport extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Global Patient ID - Links to the patient who owns this report
     */
    @NotNull(message = "Patient ID is required")
    @Column(name = "global_patient_id", nullable = false)
    private Long globalPatientId;

    /**
     * Report Type (XRAY, CT_SCAN, BLOOD_TEST, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    /**
     * AWS S3 URL for the uploaded file
     */
    @NotBlank(message = "File URL is required")
    @Column(name = "file_url", nullable = false, columnDefinition = "TEXT")
    private String fileUrl;

    /**
     * S3 Key (for deletion/management)
     */
    @Column(name = "s3_key", columnDefinition = "TEXT")
    private String s3Key;

    /**
     * File Format (IMAGE, PDF, DOC, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", nullable = false, length = 20)
    private FileFormat fileFormat;

    /**
     * Report Date
     */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    /**
     * Doctor Name (who issued/reviewed the report)
     */
    @Column(name = "doctor_name", length = 255)
    private String doctorName;

    /**
     * Lab/Hospital Name (where report was generated)
     */
    @Column(name = "lab_name", length = 255)
    private String labName;

    /**
     * AI Summary (encrypted) - AI-generated summary of the report
     */
    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary; // Will be encrypted

    /**
     * File Name (original name)
     */
    @Column(name = "file_name", length = 255)
    private String fileName;

    /**
     * File Size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Is Active (for soft delete)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Additional Notes (encrypted)
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Will be encrypted

    /**
     * Uploaded by hospital (null = uploaded by patient self; non-null = uploaded by that hospital)
     */
    @Column(name = "uploaded_by_hospital_id")
    private Long uploadedByHospitalId;
}
