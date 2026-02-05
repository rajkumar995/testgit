package com.medidropbox.service.impl;

import com.medidropbox.dto.request.GrantReportPermissionRequest;
import com.medidropbox.dto.request.LabReportUploadRequest;
import com.medidropbox.dto.response.LabReportResponse;
import com.medidropbox.dto.response.ReportPermissionResponse;
import com.medidropbox.entity.Doctor;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.LabReport;
import com.medidropbox.entity.ReportPermission;
import com.medidropbox.enums.FileFormat;
import com.medidropbox.enums.ReportType;
import com.medidropbox.repository.DoctorRepository;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.LabReportRepository;
import com.medidropbox.repository.ReportPermissionRepository;
import com.medidropbox.service.AwsS3Service;
import com.medidropbox.service.EncryptionService;
import com.medidropbox.service.FileValidationService;
import com.medidropbox.service.AuditLogService;
import com.medidropbox.service.LabReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LabReportServiceImpl implements LabReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(LabReportServiceImpl.class);
    
    private final LabReportRepository labReportRepository;
    private final ReportPermissionRepository reportPermissionRepository;
    private final AwsS3Service awsS3Service;
    private final EncryptionService encryptionService;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final FileValidationService fileValidationService;
    private final AuditLogService auditLogService;
    
    public LabReportServiceImpl(
            LabReportRepository labReportRepository,
            ReportPermissionRepository reportPermissionRepository,
            AwsS3Service awsS3Service,
            EncryptionService encryptionService,
            DoctorRepository doctorRepository,
            HospitalRepository hospitalRepository,
            FileValidationService fileValidationService,
            AuditLogService auditLogService) {
        this.labReportRepository = labReportRepository;
        this.reportPermissionRepository = reportPermissionRepository;
        this.awsS3Service = awsS3Service;
        this.encryptionService = encryptionService;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.fileValidationService = fileValidationService;
        this.auditLogService = auditLogService;
    }
    
    @Override
    public LabReportResponse uploadReport(Long globalPatientId, LabReportUploadRequest request) {
        try {
            // SECURITY: Validate file before processing
            fileValidationService.validateLabReportFile(request.getFile());
            
            // Determine file format
            FileFormat fileFormat = determineFileFormat(request.getFile().getContentType(), 
                                                         request.getFile().getOriginalFilename());
            
            // Upload to S3
            String folder = "reports/" + request.getReportType().name().toLowerCase() + "/" + globalPatientId + "/";
            String fileUrl = awsS3Service.uploadFile(request.getFile(), folder);
            String s3Key = awsS3Service.extractS3KeyFromUrl(fileUrl);
            
            // AUDIT: Log file upload
            auditLogService.logDataModification(
                globalPatientId,
                "LabReport",
                null, // Report ID not yet created
                "UPLOAD",
                String.format("File: %s, Type: %s, Size: %d bytes", 
                    request.getFile().getOriginalFilename(),
                    request.getReportType(),
                    request.getFile().getSize())
            );
            
            // Create lab report entity
            LabReport labReport = new LabReport();
            labReport.setGlobalPatientId(globalPatientId);
            labReport.setReportType(request.getReportType());
            labReport.setFileUrl(fileUrl);
            labReport.setS3Key(s3Key);
            labReport.setFileFormat(fileFormat);
            labReport.setReportDate(request.getReportDate());
            labReport.setDoctorName(request.getDoctorName());
            labReport.setLabName(request.getLabName());
            labReport.setFileName(request.getFile().getOriginalFilename());
            labReport.setFileSize(request.getFile().getSize());
            labReport.setIsActive(true);
            labReport.setUploadedByHospitalId(null); // Patient self-upload

            // Encrypt sensitive fields
            if (request.getAiSummary() != null && !request.getAiSummary().isEmpty()) {
                labReport.setAiSummary(encryptionService.encrypt(request.getAiSummary()));
            }
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                labReport.setNotes(encryptionService.encrypt(request.getNotes()));
            }
            
            LabReport saved = labReportRepository.save(labReport);
            return mapToResponse(saved);
            
        } catch (Exception e) {
            logger.error("Error uploading lab report", e);
            throw new RuntimeException("Failed to upload lab report: " + e.getMessage());
        }
    }
    
    @Override
    public List<LabReportResponse> getMyReports(Long globalPatientId) {
        List<LabReport> reports = labReportRepository.findByGlobalPatientIdAndIsActiveTrueOrderByReportDateDesc(globalPatientId);
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LabReportResponse> getMyReportsByType(Long globalPatientId, ReportType reportType) {
        List<LabReport> reports = labReportRepository.findByGlobalPatientIdAndReportTypeAndIsActiveTrueOrderByReportDateDesc(
                globalPatientId, reportType);
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public LabReportResponse getMyReportById(Long globalPatientId, Long reportId) {
        LabReport report = labReportRepository.findByIdAndGlobalPatientIdAndIsActiveTrue(reportId, globalPatientId)
                .orElseThrow(() -> new RuntimeException("Report not found or access denied"));
        return mapToResponse(report);
    }
    
    @Override
    public void deleteReport(Long globalPatientId, Long reportId) {
        LabReport report = labReportRepository.findByIdAndGlobalPatientIdAndIsActiveTrue(reportId, globalPatientId)
                .orElseThrow(() -> new RuntimeException("Report not found or access denied"));
        
        // Soft delete
        report.setIsActive(false);
        labReportRepository.save(report);
        
        // AUDIT: Log data deletion (GDPR compliance)
        auditLogService.logDataDeletion(
            globalPatientId,
            "LabReport",
            reportId,
            "User requested deletion"
        );
        
        // Optionally delete from S3 (commented for safety - uncomment if needed)
        // if (report.getS3Key() != null) {
        //     awsS3Service.deleteFile(report.getS3Key());
        // }
    }
    
    @Override
    public ReportPermissionResponse grantPermission(Long globalPatientId, GrantReportPermissionRequest request) {
        if (request.getDoctorId() == null && request.getHospitalId() == null) {
            throw new RuntimeException("Either doctor ID or hospital ID must be provided");
        }
        
        // Check if permission already exists
        ReportPermission existing = reportPermissionRepository
                .findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                        globalPatientId,
                        request.getDoctorId(),
                        request.getHospitalId())
                .orElse(null);
        
        if (existing != null) {
            // Reactivate if was revoked
            existing.setIsActive(true);
            if (request.getExpiresAt() != null) {
                existing.setExpiresAt(request.getExpiresAt());
            }
            if (request.getNotes() != null) {
                existing.setNotes(request.getNotes());
            }
            existing = reportPermissionRepository.save(existing);
        } else {
            // Create new permission
            ReportPermission permission = new ReportPermission();
            permission.setGlobalPatientId(globalPatientId);
            permission.setDoctorId(request.getDoctorId());
            permission.setHospitalId(request.getHospitalId());
            permission.setIsActive(true);
            permission.setExpiresAt(request.getExpiresAt());
            permission.setNotes(request.getNotes());
            existing = reportPermissionRepository.save(permission);
        }
        
        return mapPermissionToResponse(existing);
    }
    
    @Override
    public void revokePermission(Long globalPatientId, Long permissionId) {
        ReportPermission permission = reportPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));
        
        if (!permission.getGlobalPatientId().equals(globalPatientId)) {
            throw new RuntimeException("You can only revoke your own permissions");
        }
        
        permission.setIsActive(false);
        reportPermissionRepository.save(permission);
        
        // AUDIT: Log permission revoke
        Long targetId = permission.getDoctorId() != null ? permission.getDoctorId() : permission.getHospitalId();
        auditLogService.logPermissionChange(
            globalPatientId,
            targetId,
            "LabReport",
            "REVOKE"
        );
    }
    
    @Override
    public List<ReportPermissionResponse> getMyPermissions(Long globalPatientId) {
        List<ReportPermission> permissions = reportPermissionRepository
                .findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(globalPatientId);
        return permissions.stream()
                .map(this::mapPermissionToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LabReportResponse> getPatientReports(Long patientId, Long doctorId, Long hospitalId) {
        // Check permission - allow if doctor has permission OR hospital has permission
        boolean hasPermission = reportPermissionRepository.hasPermission(patientId, doctorId, hospitalId);
        
        if (!hasPermission) {
            // AUDIT: Log unauthorized access attempt
            Long accessorId = doctorId != null ? doctorId : hospitalId;
            auditLogService.logDataAccess(
                accessorId,
                "LabReport",
                patientId,
                "UNAUTHORIZED_ACCESS_ATTEMPT"
            );
            throw new RuntimeException("You don't have permission to view this patient's reports. Please ask the patient to grant permission from the app.");
        }
        
        // AUDIT: Log authorized data access (HIPAA compliance)
        Long accessorId = doctorId != null ? doctorId : hospitalId;
        auditLogService.logDataAccess(accessorId, "LabReport", patientId, "VIEW_ALL");
        
        List<LabReport> reports = labReportRepository.findAccessibleReportsForDoctor(
                patientId, 
                doctorId, 
                hospitalId);
        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LabReportResponse> getPatientReportsByType(Long patientId, ReportType reportType, 
                                                           Long doctorId, Long hospitalId) {
        // Check permission
        boolean hasPermission = reportPermissionRepository.hasPermission(patientId, doctorId, hospitalId);
        
        if (!hasPermission) {
            throw new RuntimeException("You don't have permission to view this patient's reports. Please ask the patient to grant permission from the app.");
        }
        
        List<LabReport> allReports = labReportRepository.findAccessibleReportsForDoctor(
                patientId, 
                doctorId, 
                hospitalId);
        return allReports.stream()
                .filter(r -> r.getReportType() == reportType)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LabReportResponse uploadReportForPatientByHospital(Long globalPatientId, Long hospitalId, LabReportUploadRequest request) {
        try {
            fileValidationService.validateLabReportFile(request.getFile());
            FileFormat fileFormat = determineFileFormat(request.getFile().getContentType(), request.getFile().getOriginalFilename());
            String folder = "reports/" + request.getReportType().name().toLowerCase() + "/" + globalPatientId + "/";
            String fileUrl = awsS3Service.uploadFile(request.getFile(), folder);
            String s3Key = awsS3Service.extractS3KeyFromUrl(fileUrl);

            auditLogService.logDataModification(
                    globalPatientId,
                    "LabReport",
                    null,
                    "UPLOAD_BY_HOSPITAL",
                    String.format("File: %s, Type: %s, HospitalId: %d", request.getFile().getOriginalFilename(), request.getReportType(), hospitalId)
            );

            LabReport labReport = new LabReport();
            labReport.setGlobalPatientId(globalPatientId);
            labReport.setReportType(request.getReportType());
            labReport.setFileUrl(fileUrl);
            labReport.setS3Key(s3Key);
            labReport.setFileFormat(fileFormat);
            labReport.setReportDate(request.getReportDate());
            labReport.setDoctorName(request.getDoctorName());
            labReport.setLabName(request.getLabName());
            labReport.setFileName(request.getFile().getOriginalFilename());
            labReport.setFileSize(request.getFile().getSize());
            labReport.setIsActive(true);
            labReport.setUploadedByHospitalId(hospitalId);

            if (request.getAiSummary() != null && !request.getAiSummary().isEmpty()) {
                labReport.setAiSummary(encryptionService.encrypt(request.getAiSummary()));
            }
            if (request.getNotes() != null && !request.getNotes().isEmpty()) {
                labReport.setNotes(encryptionService.encrypt(request.getNotes()));
            }

            LabReport saved = labReportRepository.save(labReport);
            return mapToResponse(saved);
        } catch (Exception e) {
            logger.error("Error uploading lab report for patient by hospital", e);
            throw new RuntimeException("Failed to upload lab report: " + e.getMessage());
        }
    }

    @Override
    public List<LabReportResponse> getReportsForPatientByHospital(Long globalPatientId, Long hospitalId) {
        List<LabReport> reports = labReportRepository.findByGlobalPatientIdAndUploadedByHospitalIdAndIsActiveTrueOrderByReportDateDesc(
                globalPatientId, hospitalId);
        return reports.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<LabReportResponse> getPatientReportsForHospitalContext(Long globalPatientId, Long hospitalId) {
        boolean hasPermission = reportPermissionRepository.hasPermission(globalPatientId, null, hospitalId);
        if (hasPermission) {
            return getPatientReports(globalPatientId, null, hospitalId);
        }
        return getReportsForPatientByHospital(globalPatientId, hospitalId);
    }

    private LabReportResponse mapToResponse(LabReport report) {
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
        response.setUploadedByHospitalId(report.getUploadedByHospitalId());
        response.setUploadedBy(report.getUploadedByHospitalId() == null ? "PATIENT" : "HOSPITAL");
        if (report.getUploadedByHospitalId() != null) {
            hospitalRepository.findById(report.getUploadedByHospitalId())
                    .ifPresent(h -> response.setUploadedByHospitalName(h.getName()));
        }

        // Decrypt sensitive fields
        if (report.getAiSummary() != null) {
            response.setAiSummary(encryptionService.decrypt(report.getAiSummary()));
        }
        if (report.getNotes() != null) {
            response.setNotes(encryptionService.decrypt(report.getNotes()));
        }

        return response;
    }
    
    private ReportPermissionResponse mapPermissionToResponse(ReportPermission permission) {
        ReportPermissionResponse response = new ReportPermissionResponse();
        response.setId(permission.getId());
        response.setPatientId(permission.getGlobalPatientId());
        response.setDoctorId(permission.getDoctorId());
        response.setHospitalId(permission.getHospitalId());
        response.setIsActive(permission.getIsActive());
        response.setGrantedAt(permission.getGrantedAt());
        response.setExpiresAt(permission.getExpiresAt());
        response.setNotes(permission.getNotes());
        
        // Populate doctor name if available
        if (permission.getDoctorId() != null) {
            doctorRepository.findById(permission.getDoctorId())
                    .ifPresent(doctor -> response.setDoctorName(doctor.getName()));
        }
        
        // Populate hospital name if available
        if (permission.getHospitalId() != null) {
            hospitalRepository.findById(permission.getHospitalId())
                    .ifPresent(hospital -> response.setHospitalName(hospital.getName()));
        }
        
        return response;
    }
    
    private FileFormat determineFileFormat(String contentType, String fileName) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return FileFormat.IMAGE;
            } else if (contentType.equals("application/pdf")) {
                return FileFormat.PDF;
            } else if (contentType.equals("application/msword") || 
                       contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                return FileFormat.DOCX;
            }
        }
        
        if (fileName != null) {
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".pdf")) return FileFormat.PDF;
            if (lower.endsWith(".doc")) return FileFormat.DOC;
            if (lower.endsWith(".docx")) return FileFormat.DOCX;
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
                lower.endsWith(".png") || lower.endsWith(".gif")) {
                return FileFormat.IMAGE;
            }
        }
        
        return FileFormat.OTHER;
    }
}
