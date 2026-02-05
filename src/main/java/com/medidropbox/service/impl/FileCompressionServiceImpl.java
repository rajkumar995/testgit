package com.medidropbox.service.impl;

import com.medidropbox.service.FileCompressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * File Compression Service Implementation
 * Uses GZIP compression for files before upload
 */
@Service
public class FileCompressionServiceImpl implements FileCompressionService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileCompressionServiceImpl.class);
    
    // Minimum file size to compress (1 MB) - smaller files may not benefit from compression
    @Value("${file.compression.min-size-mb:1}")
    private long minCompressionSizeMB;
    
    // Maximum file size before compression (10 MB) - files larger than this should be rejected
    @Value("${file.compression.max-size-mb:10}")
    private long maxFileSizeMB;
    
    // Compression level (0-9, where 9 is maximum compression)
    @Value("${file.compression.level:6}")
    private int compressionLevel;
    
    // Enable compression for images
    @Value("${file.compression.compress-images:true}")
    private boolean compressImages;
    
    // Enable compression for PDFs
    @Value("${file.compression.compress-pdfs:true}")
    private boolean compressPdfs;
    
    // File types that should NOT be compressed (truly already compressed formats)
    // Note: JPEG, PNG, GIF, PDF can still benefit from additional compression for storage
    private static final String[] ALREADY_COMPRESSED_TYPES = {
        "application/zip", "application/x-gzip", "application/x-compress",
        "image/webp" // WebP is already highly optimized
    };
    
    @Override
    public CompressedFileData compressFile(MultipartFile file) {
        try {
            long originalSize = file.getSize();
            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();
            
            // Check if file should be compressed
            if (!shouldCompress(originalSize, contentType)) {
                // Return original file data without compression
                byte[] originalData = file.getBytes();
                logger.debug("File {} not compressed (size: {} bytes, type: {})", 
                    fileName, originalSize, contentType);
                return new CompressedFileData(originalData, originalSize, originalSize, 
                    fileName, contentType, false);
            }
            
            // Compress the file
            byte[] originalData = file.getBytes();
            byte[] compressedData = compress(originalData);
            
            long compressedSize = compressedData.length;
            double compressionRatio = (double) compressedSize / originalSize;
            
            logger.info("File compressed: {} ({} bytes -> {} bytes, ratio: {:.2f}%)", 
                fileName, originalSize, compressedSize, compressionRatio * 100);
            
            return new CompressedFileData(compressedData, originalSize, compressedSize, 
                fileName, contentType, true);
            
        } catch (IOException e) {
            logger.error("Error compressing file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to compress file: " + e.getMessage());
        }
    }
    
    @Override
    public CompressedFileData compressFile(InputStream inputStream, String fileName, String contentType) {
        try {
            byte[] originalData = readAllBytes(inputStream);
            long originalSize = originalData.length;
            
            // Check if file should be compressed
            if (!shouldCompress(originalSize, contentType)) {
                logger.debug("File {} not compressed (size: {} bytes, type: {})", 
                    fileName, originalSize, contentType);
                return new CompressedFileData(originalData, originalSize, originalSize, 
                    fileName, contentType, false);
            }
            
            // Compress the file
            byte[] compressedData = compress(originalData);
            long compressedSize = compressedData.length;
            
            logger.info("File compressed: {} ({} bytes -> {} bytes)", 
                fileName, originalSize, compressedSize);
            
            return new CompressedFileData(compressedData, originalSize, compressedSize, 
                fileName, contentType, true);
            
        } catch (IOException e) {
            logger.error("Error compressing file: {}", fileName, e);
            throw new RuntimeException("Failed to compress file: " + e.getMessage());
        }
    }
    
    @Override
    public InputStream decompressFile(byte[] compressedData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            InflaterInputStream iis = new InflaterInputStream(bais);
            return iis;
        } catch (Exception e) {
            logger.error("Error decompressing file", e);
            throw new RuntimeException("Failed to decompress file: " + e.getMessage());
        }
    }
    
    @Override
    public boolean shouldCompress(long fileSize, String contentType) {
        // Don't compress if file is too small (unless min-size is 0)
        if (minCompressionSizeMB > 0) {
            long minSizeBytes = minCompressionSizeMB * 1024 * 1024;
            if (fileSize < minSizeBytes) {
                return false;
            }
        }
        
        // Don't compress truly already compressed formats (zip, gzip, etc.)
        if (contentType != null) {
            String lowerContentType = contentType.toLowerCase();
            for (String compressedType : ALREADY_COMPRESSED_TYPES) {
                if (lowerContentType.contains(compressedType.toLowerCase())) {
                    logger.debug("Skipping compression for already compressed format: {}", contentType);
                    return false;
                }
            }
            
            // Check image compression setting
            if (lowerContentType.startsWith("image/")) {
                if (!compressImages) {
                    logger.debug("Image compression disabled, skipping: {}", contentType);
                    return false;
                }
                // Compress images (JPEG, PNG, GIF, etc.) if enabled
                return true;
            }
            
            // Check PDF compression setting
            if (lowerContentType.equals("application/pdf")) {
                if (!compressPdfs) {
                    logger.debug("PDF compression disabled, skipping");
                    return false;
                }
                // Compress PDFs if enabled
                return true;
            }
        }
        
        // Compress all other files if they meet size requirements
        return true;
    }
    
    /**
     * Compress byte array using Deflater (GZIP compatible)
     */
    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(compressionLevel);
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater);
        
        dos.write(data);
        dos.finish();
        dos.close();
        
        return baos.toByteArray();
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

