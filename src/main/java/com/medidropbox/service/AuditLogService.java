package com.medidropbox.service;

import java.time.LocalDateTime;

/**
 * Audit Log Service
 * Logs all sensitive operations for compliance (GDPR, HIPAA)
 */
public interface AuditLogService {
    
    /**
     * Log data access
     * @param userId User ID who accessed data
     * @param resourceType Type of resource accessed (e.g., "LabReport", "Patient")
     * @param resourceId ID of resource accessed
     * @param action Action performed (e.g., "VIEW", "DOWNLOAD")
     */
    void logDataAccess(Long userId, String resourceType, Long resourceId, String action);
    
    /**
     * Log data modification
     * @param userId User ID who modified data
     * @param resourceType Type of resource modified
     * @param resourceId ID of resource modified
     * @param action Action performed (e.g., "CREATE", "UPDATE", "DELETE")
     * @param details Additional details
     */
    void logDataModification(Long userId, String resourceType, Long resourceId, 
                            String action, String details);
    
    /**
     * Log authentication event
     * @param username Username
     * @param success Whether authentication was successful
     * @param ipAddress IP address
     */
    void logAuthentication(String username, boolean success, String ipAddress);
    
    /**
     * Log permission grant/revoke
     * @param userId User ID who granted/revoked permission
     * @param targetUserId Target user ID
     * @param resourceType Resource type
     * @param action "GRANT" or "REVOKE"
     */
    void logPermissionChange(Long userId, Long targetUserId, String resourceType, String action);
    
    /**
     * Log data export
     * @param userId User ID who exported data
     * @param dataType Type of data exported
     */
    void logDataExport(Long userId, String dataType);
    
    /**
     * Log data deletion
     * @param userId User ID who deleted data
     * @param resourceType Type of resource deleted
     * @param resourceId ID of resource deleted
     * @param reason Reason for deletion
     */
    void logDataDeletion(Long userId, String resourceType, Long resourceId, String reason);
}
