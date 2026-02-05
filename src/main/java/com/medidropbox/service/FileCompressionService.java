package com.medidropbox.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * File Compression Service
 * Handles compression and decompression of files
 */
public interface FileCompressionService {
    
    /**
     * Compress file before upload
     * @param file Original file
     * @return Compressed file data with metadata
     */
    CompressedFileData compressFile(MultipartFile file);
    
    /**
     * Compress input stream
     * @param inputStream Original input stream
     * @param fileName Original file name
     * @param contentType Original content type
     * @return Compressed file data with metadata
     */
    CompressedFileData compressFile(InputStream inputStream, String fileName, String contentType);
    
    /**
     * Decompress file when downloading
     * @param compressedData Compressed file data
     * @return Decompressed input stream
     */
    InputStream decompressFile(byte[] compressedData);
    
    /**
     * Check if file should be compressed (based on size and type)
     * @param fileSize File size in bytes
     * @param contentType File content type
     * @return true if file should be compressed
     */
    boolean shouldCompress(long fileSize, String contentType);
    
    /**
     * Compressed file data container
     */
    class CompressedFileData {
        private byte[] compressedData;
        private long originalSize;
        private long compressedSize;
        private String originalFileName;
        private String contentType;
        private boolean wasCompressed;
        
        public CompressedFileData(byte[] compressedData, long originalSize, long compressedSize, 
                                 String originalFileName, String contentType, boolean wasCompressed) {
            this.compressedData = compressedData;
            this.originalSize = originalSize;
            this.compressedSize = compressedSize;
            this.originalFileName = originalFileName;
            this.contentType = contentType;
            this.wasCompressed = wasCompressed;
        }
        
        public byte[] getCompressedData() { return compressedData; }
        public long getOriginalSize() { return originalSize; }
        public long getCompressedSize() { return compressedSize; }
        public String getOriginalFileName() { return originalFileName; }
        public String getContentType() { return contentType; }
        public boolean wasCompressed() { return wasCompressed; }
        public double getCompressionRatio() {
            return originalSize > 0 ? (double) compressedSize / originalSize : 1.0;
        }
    }
}

