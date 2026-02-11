package com.medidropbox.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Barcode Generator Utility
 * Generates unique barcodes for lab samples
 */
public class BarcodeGenerator {
    
    private static final String PREFIX = "LAB";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Generate unique barcode for lab sample
     * Format: LAB-YYYYMMDD-XXXXX (where XXXXX is short UUID)
     * Example: LAB-20260205-A3F2B
     */
    public static String generateBarcode() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        String uuidPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return String.format("%s-%s-%s", PREFIX, datePart, uuidPart);
    }
    
    /**
     * Generate barcode with custom prefix
     */
    public static String generateBarcode(String prefix) {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        String uuidPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return String.format("%s-%s-%s", prefix, datePart, uuidPart);
    }
    
    /**
     * Validate barcode format
     */
    public static boolean isValidBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return false;
        }
        // Format: LAB-YYYYMMDD-XXXXX
        return barcode.matches("^LAB-\\d{8}-[A-Z0-9]{5}$");
    }
}
