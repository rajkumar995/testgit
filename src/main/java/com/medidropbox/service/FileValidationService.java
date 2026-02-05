package com.medidropbox.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * File Validation Service
 * Validates file uploads for security and compliance
 */
public interface FileValidationService {
    
    /**
     * Validate file upload
     * @param file File to validate
     * @param allowedTypes Allowed MIME types
     * @param maxSizeInMB Maximum file size in MB
     * @throws SecurityException if validation fails
     */
    void validateFile(MultipartFile file, String[] allowedTypes, long maxSizeInMB);
    
    /**
     * Validate lab report file
     * @param file File to validate
     * @throws SecurityException if validation fails
     */
    void validateLabReportFile(MultipartFile file);
    
    /**
     * Check if file type is allowed
     * @param contentType MIME type
     * @param allowedTypes Allowed types
     * @return true if allowed
     */
    boolean isAllowedFileType(String contentType, String[] allowedTypes);
    
    /**
     * Get file extension from filename
     * @param filename File name
     * @return File extension
     */
    String getFileExtension(String filename);
}
