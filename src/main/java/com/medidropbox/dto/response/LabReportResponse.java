package com.medidropbox.dto.response;

import com.medidropbox.enums.FileFormat;
import com.medidropbox.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabReportResponse {
    
    private Long id;
    private Long patientId; // Global patient ID
    private ReportType reportType;
    private String fileUrl;
    private FileFormat fileFormat;
    private LocalDate reportDate;
    private String doctorName;
    private String labName;
    private String aiSummary; // Decrypted
    private String fileName;
    private Long fileSize;
    private String notes; // Decrypted
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** null = uploaded by patient; non-null = uploaded by that hospital */
    private Long uploadedByHospitalId;
    /** "PATIENT" or "HOSPITAL" for display */
    private String uploadedBy;
    /** Hospital name when uploaded by hospital */
    private String uploadedByHospitalName;
}
