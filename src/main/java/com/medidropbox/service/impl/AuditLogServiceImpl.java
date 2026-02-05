package com.medidropbox.service.impl;

import com.medidropbox.service.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit Log Service Implementation
 * Logs all sensitive operations for compliance (GDPR, HIPAA, DPDPA)
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOG");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void logDataAccess(Long userId, String resourceType, Long resourceId, String action) {
        String logEntry = String.format(
            "[AUDIT] [%s] DATA_ACCESS | UserId: %d | ResourceType: %s | ResourceId: %d | Action: %s",
            LocalDateTime.now().format(FORMATTER),
            userId,
            resourceType,
            resourceId,
            action
        );
        auditLogger.info(logEntry);
    }
    
    @Override
    public void logDataModification(Long userId, String resourceType, Long resourceId, 
                                   String action, String details) {
        String logEntry = String.format(
            "[AUDIT] [%s] DATA_MODIFICATION | UserId: %d | ResourceType: %s | ResourceId: %d | Action: %s | Details: %s",
            LocalDateTime.now().format(FORMATTER),
            userId,
            resourceType,
            resourceId,
            action,
            details != null ? details : "N/A"
        );
        auditLogger.info(logEntry);
    }
    
    @Override
    public void logAuthentication(String username, boolean success, String ipAddress) {
        String status = success ? "SUCCESS" : "FAILED";
        String logEntry = String.format(
            "[AUDIT] [%s] AUTHENTICATION | Username: %s | Status: %s | IP: %s",
            LocalDateTime.now().format(FORMATTER),
            username,
            status,
            ipAddress != null ? ipAddress : "UNKNOWN"
        );
        auditLogger.info(logEntry);
    }
    
    @Override
    public void logPermissionChange(Long userId, Long targetUserId, String resourceType, String action) {
        String logEntry = String.format(
            "[AUDIT] [%s] PERMISSION_CHANGE | UserId: %d | TargetUserId: %d | ResourceType: %s | Action: %s",
            LocalDateTime.now().format(FORMATTER),
            userId,
            targetUserId,
            resourceType,
            action
        );
        auditLogger.info(logEntry);
    }
    
    @Override
    public void logDataExport(Long userId, String dataType) {
        String logEntry = String.format(
            "[AUDIT] [%s] DATA_EXPORT | UserId: %d | DataType: %s",
            LocalDateTime.now().format(FORMATTER),
            userId,
            dataType
        );
        auditLogger.info(logEntry);
    }
    
    @Override
    public void logDataDeletion(Long userId, String resourceType, Long resourceId, String reason) {
        String logEntry = String.format(
            "[AUDIT] [%s] DATA_DELETION | UserId: %d | ResourceType: %s | ResourceId: %d | Reason: %s",
            LocalDateTime.now().format(FORMATTER),
            userId,
            resourceType,
            resourceId,
            reason != null ? reason : "N/A"
        );
        auditLogger.info(logEntry);
    }
}
