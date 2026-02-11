package com.medidropbox.dto.response;

import com.medidropbox.enums.LabRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabRequestResponse {
    
    private Long id;
    
    private Long requestingHospitalId;
    private String requestingHospitalName;
    
    private Long requestingDoctorId;
    private String requestingDoctorName;
    
    private Long patientId;
    private String patientName;
    private String patientPhone;
    
    private Long labTestId;
    private String labTestName;
    private String labTestCode;
    
    private Long assignedLabId;
    private String assignedLabName;
    
    private LabRequestStatus status;
    
    private String barcode;
    
    private String requestNotes;
    
    private LocalDateTime sampleReceivedAt;
    private String sampleReceivedBy;
    
    private LocalDateTime testStartedAt;
    private LocalDateTime testCompletedAt;
    
    private Long labReportId;
    private String labReportUrl;
    
    private String rejectionReason;
    
    private String priority;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
