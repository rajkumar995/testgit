package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Patient Data Export Response
 * Contains all patient data from all hospitals and patient's own account
 * Used for GDPR Right to Access (Article 15)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientDataExportResponse {
    
    private Long globalPatientId;
    private LocalDateTime exportedAt;
    private String format; // JSON, PDF
    
    /**
     * Patient's true identity (GlobalPatient)
     */
    private PatientIdentity patientIdentity;
    
    /**
     * Data from all hospitals
     */
    private List<HospitalData> hospitalData;
    
    /**
     * Patient's own data (lab reports, permissions)
     */
    private PatientOwnData patientOwnData;
    
    /**
     * Activity logs
     */
    private List<ActivityLog> activityLogs;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientIdentity {
        private String fullName;
        private String phone;
        private String email;
        private String aadharId;
        private String abhaId;
        private AddressResponse address;
        private List<EmergencyContactResponse> emergencyContacts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospitalData {
        private Long hospitalId;
        private String hospitalName;
        private HospitalPatientProfile hospitalPatientProfile;
        private List<BookingResponse> bookings;
        private List<QueueResponse> queueEntries;
        private List<Map<String, Object>> payments;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospitalPatientProfile {
        private Long id;
        private String fullName;
        private String phone;
        private String email;
        private String notes;
        private Boolean dataConsent;
        private AddressResponse address;
        private LocalDateTime createdAt;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientOwnData {
        private List<LabReportResponse> labReports;
        private List<ReportPermissionResponse> permissions;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityLog {
        private String action;
        private String resourceType;
        private Long resourceId;
        private LocalDateTime timestamp;
        private String details;
    }
    
    // Reuse existing DTOs
    private AddressResponse address;
    private EmergencyContactResponse emergencyContact;
}
