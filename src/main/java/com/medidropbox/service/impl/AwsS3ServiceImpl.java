package com.medidropbox.service.impl;

import com.medidropbox.config.AwsS3Config;
import com.medidropbox.service.AwsS3Service;
import com.medidropbox.service.FileCompressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
public class AwsS3ServiceImpl implements AwsS3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(AwsS3ServiceImpl.class);
    
    private final AwsS3Config awsConfig;
    private final FileCompressionService compressionService;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${aws.s3.base-url}")
    private String baseUrl;
    
    public AwsS3ServiceImpl(AwsS3Config awsConfig, FileCompressionService compressionService, 
                           S3Client s3Client, S3Presigner s3Presigner) {
        this.awsConfig = awsConfig;
        this.compressionService = compressionService;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        logger.info("AwsS3ServiceImpl initialized with bucket: {}, region: {}", 
                awsConfig.getBucketName(), awsConfig.getRegion());
    }
    
    private String getBucketName() {
        return awsConfig.getBucketName();
    }
    
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Compress file before upload
            FileCompressionService.CompressedFileData compressedData = compressionService.compressFile(file);
            
            String fileName = generateFileName(file.getOriginalFilename());
            String s3Key = folder + fileName;
            
            // Determine content type - if compressed, use application/octet-stream
            String contentType = compressedData.wasCompressed() 
                ? "application/octet-stream" 
                : compressedData.getContentType();
            
            // Add metadata to track compression
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(s3Key)
                    .contentType(contentType)
                    .serverSideEncryption(ServerSideEncryption.AES256);
            
            // Add compression metadata
            if (compressedData.wasCompressed()) {
                requestBuilder.metadata(java.util.Map.of(
                    "original-size", String.valueOf(compressedData.getOriginalSize()),
                    "compressed-size", String.valueOf(compressedData.getCompressedSize()),
                    "compression-ratio", String.format("%.2f", compressedData.getCompressionRatio()),
                    "original-content-type", compressedData.getContentType(),
                    "is-compressed", "true"
                ));
            }
            
            PutObjectRequest putObjectRequest = requestBuilder.build();
            
            // Upload compressed data
            s3Client.putObject(putObjectRequest, 
                RequestBody.fromBytes(compressedData.getCompressedData()));
            
            String fileUrl = baseUrl + "/" + s3Key;
            if (compressedData.wasCompressed()) {
                logger.info("Compressed file uploaded to S3: {} ({} bytes -> {} bytes, ratio: {:.2f}%)", 
                    fileUrl, compressedData.getOriginalSize(), compressedData.getCompressedSize(),
                    compressedData.getCompressionRatio() * 100);
            } else {
                logger.info("File uploaded to S3: {} ({} bytes)", fileUrl, compressedData.getOriginalSize());
            }
            return fileUrl;
            
        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadFile(InputStream inputStream, String fileName, String folder, String contentType) {
        try {
            // Compress file before upload
            FileCompressionService.CompressedFileData compressedData = 
                compressionService.compressFile(inputStream, fileName, contentType);
            
            String s3FileName = generateFileName(fileName);
            String s3Key = folder + s3FileName;
            
            // Determine content type - if compressed, use application/octet-stream
            String uploadContentType = compressedData.wasCompressed() 
                ? "application/octet-stream" 
                : compressedData.getContentType();
            
            // Add metadata to track compression
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(s3Key)
                    .contentType(uploadContentType)
                    .serverSideEncryption(ServerSideEncryption.AES256);
            
            // Add compression metadata
            if (compressedData.wasCompressed()) {
                requestBuilder.metadata(java.util.Map.of(
                    "original-size", String.valueOf(compressedData.getOriginalSize()),
                    "compressed-size", String.valueOf(compressedData.getCompressedSize()),
                    "compression-ratio", String.format("%.2f", compressedData.getCompressionRatio()),
                    "original-content-type", compressedData.getContentType(),
                    "is-compressed", "true"
                ));
            }
            
            PutObjectRequest putObjectRequest = requestBuilder.build();
            
            // Upload compressed data
            s3Client.putObject(putObjectRequest, 
                RequestBody.fromBytes(compressedData.getCompressedData()));
            
            String fileUrl = baseUrl + "/" + s3Key;
            if (compressedData.wasCompressed()) {
                logger.info("Compressed file uploaded to S3: {} ({} bytes -> {} bytes, ratio: {:.2f}%)", 
                    fileUrl, compressedData.getOriginalSize(), compressedData.getCompressedSize(),
                    compressedData.getCompressionRatio() * 100);
            } else {
                logger.info("File uploaded to S3: {} ({} bytes)", fileUrl, compressedData.getOriginalSize());
            }
            return fileUrl;
            
        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(byte[] data, String fileName, String folder, String contentType) {
        try {
            String s3FileName = generateFileName(fileName);
            String s3Key = folder + s3FileName;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(s3Key)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .contentLength((long) data.length)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));
            String fileUrl = baseUrl + "/" + s3Key;
            logger.info("File uploaded to S3 (no compression): {} ({} bytes)", fileUrl, data.length);
            return fileUrl;
        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(s3Key)
                    .build();
            
            s3Client.deleteObject(deleteObjectRequest);
            logger.info("File deleted from S3: {}", s3Key);
            
        } catch (Exception e) {
            logger.error("Error deleting file from S3: {}", s3Key, e);
            throw new RuntimeException("Failed to delete file from S3: " + e.getMessage());
        }
    }
    
    @Override
    public String generatePresignedUrl(String s3Key, int expirationMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(s3Key)
                    .build();
            
            PresignedGetObjectRequest presignedRequest = s3Presigner
                    .presignGetObject(b -> b.signatureDuration(Duration.ofMinutes(expirationMinutes))
                            .getObjectRequest(getObjectRequest));
            
            return presignedRequest.url().toString();
            
        } catch (Exception e) {
            logger.error("Error generating presigned URL for: {}", s3Key, e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage());
        }
    }
    
    @Override
    public String extractS3KeyFromUrl(String url) {
        try {
            URL urlObj = new URL(url);
            String path = urlObj.getPath();
            // Remove leading slash
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            logger.error("Error extracting S3 key from URL: {}", url, e);
            return null;
        }
    }
    
    @Override
    public InputStream downloadAndDecompressFile(String s3Key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(getBucketName())
                    .key(s3Key)
                    .build();
            
            software.amazon.awssdk.core.ResponseInputStream<GetObjectResponse> response = 
                s3Client.getObject(getObjectRequest);
            
            // Check if file is compressed from metadata
            String isCompressed = response.response().metadata().get("is-compressed");
            
            if ("true".equals(isCompressed)) {
                // Read compressed data
                byte[] compressedData = readAllBytes(response);
                // Decompress
                return compressionService.decompressFile(compressedData);
            } else {
                // Return original stream if not compressed
                byte[] data = readAllBytes(response);
                return new java.io.ByteArrayInputStream(data);
            }
            
        } catch (Exception e) {
            logger.error("Error downloading and decompressing file from S3: {}", s3Key, e);
            throw new RuntimeException("Failed to download file from S3: " + e.getMessage());
        }
    }
    
    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    /**
     * Read all bytes from input stream (Java 8 compatible)
     */
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
