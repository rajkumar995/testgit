package com.medidropbox.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * AWS S3 Service for file uploads
 */
public interface AwsS3Service {
    
    /**
     * Upload file to S3
     * @param file MultipartFile to upload
     * @param folder Folder path in S3 (e.g., "reports/xray/")
     * @return S3 URL of uploaded file
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * Upload file from InputStream
     * @param inputStream File input stream
     * @param fileName File name
     * @param folder Folder path in S3
     * @param contentType Content type
     * @return S3 URL of uploaded file
     */
    String uploadFile(InputStream inputStream, String fileName, String folder, String contentType);

    /**
     * Upload raw bytes to S3 without compression (e.g. invoice PDF).
     * @param data File bytes
     * @param fileName File name
     * @param folder Folder path in S3 (e.g. "invoices/")
     * @param contentType Content type (e.g. "application/pdf")
     * @return S3 URL of uploaded file
     */
    String uploadFile(byte[] data, String fileName, String folder, String contentType);
    
    /**
     * Delete file from S3
     * @param s3Key S3 key (path) of the file
     */
    void deleteFile(String s3Key);
    
    /**
     * Generate pre-signed URL for temporary access
     * @param s3Key S3 key
     * @param expirationMinutes Expiration time in minutes
     * @return Pre-signed URL
     */
    String generatePresignedUrl(String s3Key, int expirationMinutes);
    
    /**
     * Get S3 key from URL
     * @param url S3 URL
     * @return S3 key
     */
    String extractS3KeyFromUrl(String url);
    
    /**
     * Download and decompress file from S3
     * @param s3Key S3 key of the file
     * @return Decompressed input stream
     */
    InputStream downloadAndDecompressFile(String s3Key);
}
