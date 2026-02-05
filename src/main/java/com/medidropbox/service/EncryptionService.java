package com.medidropbox.service;

/**
 * Encryption Service for sensitive data
 */
public interface EncryptionService {
    
    /**
     * Encrypt sensitive data
     * @param plainText Plain text to encrypt
     * @return Encrypted text
     */
    String encrypt(String plainText);
    
    /**
     * Decrypt encrypted data
     * @param encryptedText Encrypted text
     * @return Decrypted plain text
     */
    String decrypt(String encryptedText);
    
    /**
     * Check if text is encrypted
     * @param text Text to check
     * @return true if encrypted
     */
    boolean isEncrypted(String text);
}
