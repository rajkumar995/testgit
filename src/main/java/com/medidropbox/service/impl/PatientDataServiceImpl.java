package com.medidropbox.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medidropbox.dto.response.*;
import com.medidropbox.entity.Address;
import com.medidropbox.entity.ArchivedPatient;
import com.medidropbox.entity.Booking;
import com.medidropbox.entity.EmergencyContact;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.HospitalPatient;
import com.medidropbox.entity.LabReport;
import com.medidropbox.entity.Payment;
import com.medidropbox.entity.Queue;
import com.medidropbox.entity.ReportPermission;
import com.medidropbox.repository.*;
import com.medidropbox.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Patient Data Service Implementation
 * Handles GDPR/DPDPA compliance: data export, deletion, portability
 */
@Service
@Transactional
public class PatientDataServiceImpl implements PatientDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(PatientDataServiceImpl.class);
    
    private final GlobalPatientRepository globalPatientRepository;
    private final HospitalPatientRepository hospitalPatientRepository;
    private final BookingRepository bookingRepository;
    private final QueueRepository queueRepository;
    private final PaymentRepository paymentRepository;
    private final LabReportRepository labReportRepository;
    private final ReportPermissionRepository reportPermissionRepository;
    private final HospitalRepository hospitalRepository;
    private final ArchivedPatientRepository archivedPatientRepository;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;
    private final AwsS3Service awsS3Service;
    private final ObjectMapper objectMapper;
    
    public PatientDataServiceImpl(
            GlobalPatientRepository globalPatientRepository,
            HospitalPatientRepository hospitalPatientRepository,
            BookingRepository bookingRepository,
            QueueRepository queueRepository,
            PaymentRepository paymentRepository,
            LabReportRepository labReportRepository,
            ReportPermissionRepository reportPermissionRepository,
            HospitalRepository hospitalRepository,
            ArchivedPatientRepository archivedPatientRepository,
            EncryptionService encryptionService,
            AuditLogService auditLogService,
            AwsS3Service awsS3Service,
            ObjectMapper objectMapper) {
        this.globalPatientRepository = globalPatientRepository;
        this.hospitalPatientRepository = hospitalPatientRepository;
        this.bookingRepository = bookingRepository;
        this.queueRepository = queueRepository;
        this.paymentRepository = paymentRepository;
        this.labReportRepository = labReportRepository;
        this.reportPermissionRepository = reportPermissionRepository;
        this.hospitalRepository = hospitalRepository;
        this.archivedPatientRepository = archivedPatientRepository;
        this.encryptionService = encryptionService;
        this.auditLogService = auditLogService;
        this.awsS3Service = awsS3Service;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public PatientDataExportResponse exportPatientData(Long globalPatientId) {
        logger.info("Exporting patient data for globalPatientId: {}", globalPatientId);
        
        // Get patient identity
        GlobalPatient globalPatient = globalPatientRepository.findByIdWithAddressAndContacts(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Build response
        PatientDataExportResponse response = new PatientDataExportResponse();
        response.setGlobalPatientId(globalPatientId);
        response.setExportedAt(LocalDateTime.now());
        response.setFormat("JSON");
        
        // Patient identity
        response.setPatientIdentity(mapPatientIdentity(globalPatient));
        
        // Hospital data from all hospitals
        List<HospitalPatient> hospitalPatients = hospitalPatientRepository.findByGlobalPatientId(globalPatientId);
        response.setHospitalData(hospitalPatients.stream()
                .map(this::mapHospitalData)
                .collect(Collectors.toList()));
        
        // Patient's own data (lab reports, permissions)
        response.setPatientOwnData(mapPatientOwnData(globalPatientId));
        
        // Activity logs (simplified - would need audit log repository)
        response.setActivityLogs(new ArrayList<>()); // TODO: Implement audit log retrieval
        
        // Audit log
        auditLogService.logDataExport(globalPatientId, "PATIENT_DATA_EXPORT");
        
        return response;
    }
    
    @Override
    public DeletionResponse deletePatientData(Long globalPatientId, String reason) {
        logger.info("Processing deletion request for globalPatientId: {}, reason: {}", globalPatientId, reason);
        
        GlobalPatient globalPatient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // CHECK 1: Legal Hold
        if (Boolean.TRUE.equals(globalPatient.getLegalHold())) {
            logger.warn("Deletion rejected: Legal hold active for globalPatientId: {}", globalPatientId);
            auditLogService.logDataModification(globalPatientId, "PatientData", globalPatientId, 
                "DELETION_REJECTED", "Legal hold active: " + globalPatient.getLegalHoldReason());
            return DeletionResponse.rejected(
                "Cannot delete data. Legal hold is active.",
                "LEGAL_HOLD",
                globalPatient.getLegalHoldReason()
            );
        }
        
        // CHECK 2: Minimum Retention Period
        if (isWithinRetentionPeriod(globalPatient)) {
            int yearsRemaining = getYearsRemaining(globalPatient);
            logger.warn("Deletion rejected: Retention period not met for globalPatientId: {}", globalPatientId);
            auditLogService.logDataModification(globalPatientId, "PatientData", globalPatientId,
                "DELETION_REJECTED", "Retention period not met: " + yearsRemaining + " years remaining");
            return DeletionResponse.rejected(
                "Cannot delete data. Minimum retention period not met.",
                "RETENTION_PERIOD",
                "Data must be retained for " + yearsRemaining + " more years."
            );
        }
        
        // CHECK 3: Legal Obligations (always archive for healthcare)
        // Healthcare data must be retained for legal compliance
        String countryCode = getCountryCode(); // Get from hospital or default
        int retentionYears = getRetentionPeriodForCountry(countryCode);
        LocalDateTime archiveExpiresAt = LocalDateTime.now().plusYears(retentionYears);
        String legalBasis = getLegalBasisForCountry(countryCode);
        
        // Archive data
        archivePatientData(globalPatientId, reason, archiveExpiresAt, legalBasis, countryCode);
        
        // Soft delete from main database
        List<Long> deletedFromHospitals = softDeleteFromHospitals(globalPatientId);
        
        // Soft delete patient's own data
        softDeletePatientOwnData(globalPatientId);
        
        // Mark global patient as inactive
        globalPatient.setIsActive(false);
        globalPatientRepository.save(globalPatient);
        
        // Audit log
        auditLogService.logDataDeletion(globalPatientId, "PatientData", globalPatientId, reason);
        
        logger.info("Patient data archived for globalPatientId: {}, expires at: {}", globalPatientId, archiveExpiresAt);
        
        return DeletionResponse.archived(
            "Your data has been removed from active systems and archived for legal compliance.",
            archiveExpiresAt,
            "Legal obligation to retain health records for " + retentionYears + " years",
            legalBasis
        );
    }
    
    @Override
    public String exportPatientDataPortable(Long globalPatientId) {
        logger.info("Exporting portable data for globalPatientId: {}", globalPatientId);
        
        PatientDataExportResponse data = exportPatientData(globalPatientId);
        
        // Create portable format (machine-readable JSON)
        Map<String, Object> portable = new HashMap<>();
        portable.put("format", "JSON");
        portable.put("version", "1.0");
        portable.put("exportedAt", data.getExportedAt());
        portable.put("data", data);
        
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(portable);
            auditLogService.logDataExport(globalPatientId, "PORTABLE_DATA_EXPORT");
            return json;
        } catch (Exception e) {
            logger.error("Error creating portable data export", e);
            throw new RuntimeException("Failed to create portable data export", e);
        }
    }
    
    @Override
    public PatientDataExportResponse getArchivedPatientData(Long originalGlobalPatientId) {
        logger.info("Retrieving archived data for originalGlobalPatientId: {}", originalGlobalPatientId);
        
        ArchivedPatient archive = archivedPatientRepository.findByOriginalGlobalPatientId(originalGlobalPatientId)
                .orElseThrow(() -> new RuntimeException("Archived data not found"));
        
        if (Boolean.TRUE.equals(archive.getPermanentlyDeleted())) {
            throw new RuntimeException("Archived data has been permanently deleted");
        }
        
        if (archive.getArchiveExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Archive has expired and data has been permanently deleted");
        }
        
        try {
            // Decrypt archived data
            String decrypted = encryptionService.decrypt(archive.getArchivedData());
            return objectMapper.readValue(decrypted, PatientDataExportResponse.class);
        } catch (Exception e) {
            logger.error("Error retrieving archived data", e);
            throw new RuntimeException("Failed to retrieve archived data", e);
        }
    }
    
    // Helper methods
    
    private PatientDataExportResponse.PatientIdentity mapPatientIdentity(GlobalPatient patient) {
        PatientDataExportResponse.PatientIdentity identity = new PatientDataExportResponse.PatientIdentity();
        identity.setFullName(patient.getFullName());
        identity.setPhone(patient.getPhone());
        identity.setEmail(patient.getEmail());
        identity.setAadharId(patient.getAadharId());
        identity.setAbhaId(patient.getAbhaId());
        identity.setCreatedAt(patient.getCreatedAt());
        identity.setUpdatedAt(patient.getUpdatedAt());
        
        // Map address if exists
        if (patient.getAddress() != null) {
            identity.setAddress(mapAddress(patient.getAddress()));
        }
        
        // Map emergency contacts
        if (patient.getEmergencyContacts() != null) {
            identity.setEmergencyContacts(patient.getEmergencyContacts().stream()
                    .map(this::mapEmergencyContact)
                    .collect(Collectors.toList()));
        }
        
        return identity;
    }
    
    private PatientDataExportResponse.HospitalData mapHospitalData(HospitalPatient hospitalPatient) {
        PatientDataExportResponse.HospitalData hospitalData = new PatientDataExportResponse.HospitalData();
        hospitalData.setHospitalId(hospitalPatient.getHospital().getId());
        hospitalData.setHospitalName(hospitalPatient.getHospital().getName());
        
        // Hospital patient profile
        PatientDataExportResponse.HospitalPatientProfile profile = 
            new PatientDataExportResponse.HospitalPatientProfile();
        profile.setId(hospitalPatient.getId());
        profile.setFullName(hospitalPatient.getFullName());
        profile.setPhone(hospitalPatient.getPhone());
        profile.setEmail(hospitalPatient.getEmail());
        profile.setNotes(hospitalPatient.getNotes());
        profile.setDataConsent(hospitalPatient.getDataConsent());
        profile.setCreatedAt(hospitalPatient.getCreatedAt());
        if (hospitalPatient.getAddress() != null) {
            profile.setAddress(mapAddress(hospitalPatient.getAddress()));
        }
        hospitalData.setHospitalPatientProfile(profile);
        
        // Bookings
        List<Booking> bookings = bookingRepository.findByGlobalPatientId(hospitalPatient.getGlobalPatient().getId());
        hospitalData.setBookings(bookings.stream()
                .map(this::mapBooking)
                .collect(Collectors.toList()));
        
        // Queue entries
        List<Queue> queues = queueRepository.findByGlobalPatientId(hospitalPatient.getGlobalPatient().getId());
        hospitalData.setQueueEntries(queues.stream()
                .map(this::mapQueue)
                .collect(Collectors.toList()));
        
        // Payments (from bookings)
        List<Payment> payments = bookings.stream()
                .filter(b -> b.getPayment() != null)
                .map(Booking::getPayment)
                .collect(Collectors.toList());
        hospitalData.setPayments(payments.stream()
                .map(this::mapPayment)
                .collect(Collectors.toList()));
        
        return hospitalData;
    }
    
    private PatientDataExportResponse.PatientOwnData mapPatientOwnData(Long globalPatientId) {
        PatientDataExportResponse.PatientOwnData ownData = 
            new PatientDataExportResponse.PatientOwnData();
        
        // Lab reports
        List<LabReport> reports = labReportRepository.findByGlobalPatientIdAndIsActiveTrueOrderByReportDateDesc(globalPatientId);
        ownData.setLabReports(reports.stream()
                .map(this::mapLabReport)
                .collect(Collectors.toList()));
        
        // Permissions
        List<ReportPermission> permissions = reportPermissionRepository
                .findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(globalPatientId);
        ownData.setPermissions(permissions.stream()
                .map(this::mapReportPermission)
                .collect(Collectors.toList()));
        
        return ownData;
    }
    
    private void archivePatientData(Long globalPatientId, String reason, 
                                   LocalDateTime expiresAt, String legalBasis, String countryCode) {
        try {
            // Export all data
            PatientDataExportResponse data = exportPatientData(globalPatientId);
            
            // Convert to JSON
            String jsonData = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(data);
            
            // Encrypt
            String encrypted = encryptionService.encrypt(jsonData);
            
            // Create archive
            ArchivedPatient archive = new ArchivedPatient();
            archive.setOriginalGlobalPatientId(globalPatientId);
            archive.setArchivedData(encrypted);
            archive.setArchivedAt(LocalDateTime.now());
            archive.setDeletedBy("PATIENT_REQUEST");
            archive.setDeletionReason(reason);
            archive.setArchiveExpiresAt(expiresAt);
            archive.setLegalBasis(legalBasis);
            archive.setCountryCode(countryCode);
            archive.setPermanentlyDeleted(false);
            
            archivedPatientRepository.save(archive);
            
            logger.info("Patient data archived successfully for globalPatientId: {}", globalPatientId);
        } catch (Exception e) {
            logger.error("Error archiving patient data", e);
            throw new RuntimeException("Failed to archive patient data", e);
        }
    }
    
    private List<Long> softDeleteFromHospitals(Long globalPatientId) {
        List<HospitalPatient> hospitalPatients = hospitalPatientRepository.findByGlobalPatientId(globalPatientId);
        List<Long> hospitalIds = new ArrayList<>();
        
        for (HospitalPatient hp : hospitalPatients) {
            hospitalIds.add(hp.getHospital().getId());
            
            // Soft delete bookings
            List<Booking> bookings = bookingRepository.findByGlobalPatientId(globalPatientId);
            bookings.forEach(b -> {
                b.setStatus(com.medidropbox.enums.BookingStatus.CANCELLED);
                bookingRepository.save(b);
            });
            
            // Soft delete hospital patient
            hp.setIsActive(false);
            hospitalPatientRepository.save(hp);
        }
        
        return hospitalIds;
    }
    
    private void softDeletePatientOwnData(Long globalPatientId) {
        // Soft delete lab reports
        List<LabReport> reports = labReportRepository.findByGlobalPatientIdAndIsActiveTrueOrderByReportDateDesc(globalPatientId);
        reports.forEach(r -> {
            r.setIsActive(false);
            labReportRepository.save(r);
        });
        
        // Soft delete permissions
        List<ReportPermission> permissions = reportPermissionRepository
                .findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(globalPatientId);
        permissions.forEach(p -> {
            p.setIsActive(false);
            reportPermissionRepository.save(p);
        });
    }
    
    private boolean isWithinRetentionPeriod(GlobalPatient patient) {
        LocalDateTime lastServiceDate = getLastServiceDate(patient.getId());
        int retentionYears = getRetentionPeriodForCountry(getCountryCode());
        
        long yearsSinceLastService = ChronoUnit.YEARS.between(lastServiceDate, LocalDateTime.now());
        return yearsSinceLastService < retentionYears;
    }
    
    private int getYearsRemaining(GlobalPatient patient) {
        LocalDateTime lastServiceDate = getLastServiceDate(patient.getId());
        int retentionYears = getRetentionPeriodForCountry(getCountryCode());
        
        long yearsSinceLastService = ChronoUnit.YEARS.between(lastServiceDate, LocalDateTime.now());
        return (int) (retentionYears - yearsSinceLastService);
    }
    
    private LocalDateTime getLastServiceDate(Long globalPatientId) {
        List<Booking> bookings = bookingRepository.findByGlobalPatientId(globalPatientId);
        if (bookings.isEmpty()) {
            GlobalPatient patient = globalPatientRepository.findById(globalPatientId).orElse(null);
            return patient != null && patient.getCreatedAt() != null 
                ? patient.getCreatedAt() 
                : LocalDateTime.now();
        }
        return bookings.stream()
                .map(b -> b.getBookingDate().atStartOfDay())
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }
    
    private int getRetentionPeriodForCountry(String countryCode) {
        switch (countryCode.toUpperCase()) {
            case "USA":
                return 6; // HIPAA requirement
            case "IND":
            case "IN":
                return 7; // India medical records act
            case "EU":
            case "GB":
            case "UK":
                return 10; // EU/GDPR
            default:
                return 6; // Default 6 years
        }
    }
    
    private String getLegalBasisForCountry(String countryCode) {
        switch (countryCode.toUpperCase()) {
            case "USA":
                return "HIPAA_RETENTION_REQUIREMENT";
            case "IND":
            case "IN":
                return "DPDPA_LEGAL_OBLIGATION";
            case "EU":
            case "GB":
            case "UK":
                return "GDPR_LEGAL_OBLIGATION";
            default:
                return "HEALTH_RECORDS_RETENTION_LAW";
        }
    }
    
    private String getCountryCode() {
        // TODO: Get from hospital settings or system configuration
        // For now, default to "USA" (can be configured)
        return "USA";
    }
    
    // Mapping helper methods
    private AddressResponse mapAddress(Address address) {
        AddressResponse response = new AddressResponse();
        response.setAddressLine1(address.getAddressLine1());
        response.setAddressLine2(address.getAddressLine2());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setPincode(address.getPincode());
        response.setCountry(address.getCountry());
        response.setLatitude(address.getLatitude());
        response.setLongitude(address.getLongitude());
        return response;
    }
    
    private EmergencyContactResponse mapEmergencyContact(EmergencyContact contact) {
        EmergencyContactResponse response = new EmergencyContactResponse();
        response.setPersonName(contact.getPersonName());
        response.setPhone(contact.getPhone());
        response.setRelationship(contact.getRelationship());
        response.setEmail(contact.getEmail());
        response.setIsPrimary(contact.getIsPrimary());
        return response;
    }
    
    private BookingResponse mapBooking(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setDoctorId(booking.getDoctor().getId());
        response.setDoctorName(booking.getDoctor().getName());
        response.setHospitalId(booking.getHospital().getId());
        response.setHospitalName(booking.getHospital().getName());
        response.setBookingDate(booking.getBookingDate());
        response.setBookingTime(booking.getBookingTime());
        response.setStatus(booking.getStatus());
        if (booking.getPayment() != null) {
            response.setPayment(mapPaymentInfo(booking.getPayment()));
        }
        return response;
    }
    
    private QueueResponse mapQueue(Queue queue) {
        QueueResponse response = new QueueResponse();
        response.setId(queue.getId());
        response.setBookingDate(queue.getBookingDate());
        response.setQueueNumber(queue.getQueueNumber());
        response.setStatus(queue.getStatus());
        if (queue.getDoctor() != null) {
            response.setDoctorId(queue.getDoctor().getId());
            response.setDoctorName(queue.getDoctor().getName());
        }
        return response;
    }
    
    private Map<String, Object> mapPayment(Payment payment) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", payment.getId());
        response.put("paymentDate", payment.getPaymentDate());
        response.put("paymentMode", payment.getPaymentMode());
        response.put("totalBill", payment.getTotalBill());
        response.put("totalAmount", payment.getTotalAmount());
        response.put("discount", payment.getDiscount());
        response.put("taxableAmount", payment.getTaxableAmount());
        response.put("gst", payment.getGst());
        response.put("transactionId", payment.getTransactionId());
        return response;
    }
    
    private BookingResponse.PaymentInfo mapPaymentInfo(Payment payment) {
        BookingResponse.PaymentInfo info = new BookingResponse.PaymentInfo();
        info.setId(payment.getId());
        info.setTotalAmount(payment.getTotalAmount());
        info.setPaymentMode(payment.getPaymentMode() != null ? payment.getPaymentMode().name() : null);
        info.setTotalBill(payment.getTotalBill());
        info.setDiscount(payment.getDiscount());
        info.setTaxableAmount(payment.getTaxableAmount());
        info.setGst(payment.getGst());
        info.setTransactionId(payment.getTransactionId());
        info.setInvoiceUrl(payment.getInvoiceUrl());
        return info;
    }
    
    private LabReportResponse mapLabReport(LabReport report) {
        LabReportResponse response = new LabReportResponse();
        response.setId(report.getId());
        response.setPatientId(report.getGlobalPatientId());
        response.setReportType(report.getReportType());
        response.setFileUrl(report.getFileUrl());
        response.setFileFormat(report.getFileFormat());
        response.setReportDate(report.getReportDate());
        response.setDoctorName(report.getDoctorName());
        response.setLabName(report.getLabName());
        response.setFileName(report.getFileName());
        response.setFileSize(report.getFileSize());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        // Decrypt sensitive fields
        if (report.getAiSummary() != null) {
            response.setAiSummary(encryptionService.decrypt(report.getAiSummary()));
        }
        if (report.getNotes() != null) {
            response.setNotes(encryptionService.decrypt(report.getNotes()));
        }
        return response;
    }
    
    private ReportPermissionResponse mapReportPermission(ReportPermission permission) {
        ReportPermissionResponse response = new ReportPermissionResponse();
        response.setId(permission.getId());
        response.setPatientId(permission.getGlobalPatientId());
        response.setDoctorId(permission.getDoctorId());
        response.setHospitalId(permission.getHospitalId());
        response.setIsActive(permission.getIsActive());
        response.setGrantedAt(permission.getGrantedAt());
        response.setExpiresAt(permission.getExpiresAt());
        response.setNotes(permission.getNotes());
        return response;
    }
}
