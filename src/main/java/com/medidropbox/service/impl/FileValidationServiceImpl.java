package com.medidropbox.service.impl;

import com.medidropbox.service.FileValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * File Validation Service Implementation
 * Validates file uploads for security and compliance
 */
@Service
public class FileValidationServiceImpl implements FileValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileValidationServiceImpl.class);
    
    // Allowed MIME types for lab reports
    private static final List<String> ALLOWED_LAB_REPORT_TYPES = Arrays.asList(
        // Images
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp",
        // Documents
        "application/pdf",
        "application/msword", // .doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    );
    
    // Allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",
        ".pdf",
        ".doc", ".docx"
    );
    
    // Maximum file size: 10 MB
    private static final long MAX_FILE_SIZE_MB = 10;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    
    @Override
    public void validateFile(MultipartFile file, String[] allowedTypes, long maxSizeInMB) {
        if (file == null || file.isEmpty()) {
            throw new SecurityException("File is required and cannot be empty");
        }
        
        // Check file size
        long maxSizeBytes = maxSizeInMB * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new SecurityException(
                String.format("File size exceeds maximum allowed size of %d MB", maxSizeInMB)
            );
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedFileType(contentType, allowedTypes)) {
            throw new SecurityException(
                String.format("File type '%s' is not allowed. Allowed types: %s", 
                    contentType, Arrays.toString(allowedTypes))
            );
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new SecurityException(
                    String.format("File extension '%s' is not allowed", extension)
                );
            }
        }
        
        // Additional security: Check for double extension (e.g., file.pdf.exe)
        if (filename != null && filename.contains("..")) {
            throw new SecurityException("Invalid filename: path traversal detected");
        }
        
        logger.info("File validation passed: {} ({} bytes, type: {})", 
            filename, file.getSize(), contentType);
    }
    
    @Override
    public void validateLabReportFile(MultipartFile file) {
        String[] allowedTypes = ALLOWED_LAB_REPORT_TYPES.toArray(new String[0]);
        validateFile(file, allowedTypes, MAX_FILE_SIZE_MB);
    }
    
    @Override
    public boolean isAllowedFileType(String contentType, String[] allowedTypes) {
        if (contentType == null) {
            return false;
        }
        return Arrays.asList(allowedTypes).contains(contentType.toLowerCase());
    }
    
    @Override
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return filename.substring(lastDotIndex);
    }
}
