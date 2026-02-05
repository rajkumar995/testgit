package com.medidropbox.service.impl;

import com.medidropbox.service.EncryptionService;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EncryptionServiceImpl implements EncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(EncryptionServiceImpl.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String ENCRYPTION_PREFIX = "ENC:";
    
    @Value("${jasypt.encryptor.password}")
    private String encryptionPassword;
    
    private StringEncryptor encryptor;
    
    private StringEncryptor getEncryptor() {
        if (encryptor == null) {
            PooledPBEStringEncryptor pooledEncryptor = new PooledPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPassword(encryptionPassword);
            // Upgraded to stronger algorithm: PBEWITHHMACSHA512ANDAES_256 (AES-256)
            // This is compliant with HIPAA, GDPR, and other international standards
            config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
            config.setKeyObtentionIterations(10000); // Increased iterations for better security
            config.setPoolSize(1);
            config.setProviderName("SunJCE");
            config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
            config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator"); // Add IV for AES
            config.setStringOutputType("base64");
            pooledEncryptor.setConfig(config);
            encryptor = pooledEncryptor;
        }
        return encryptor;
    }
    
    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        if (isEncrypted(plainText)) {
            return plainText; // Already encrypted
        }
        
        try {
            String encrypted = getEncryptor().encrypt(plainText);
            return ENCRYPTION_PREFIX + encrypted;
        } catch (Exception e) {
            logger.error("Error encrypting text", e);
            return plainText; // Return original on error
        }
    }
    
    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        if (!isEncrypted(encryptedText)) {
            return encryptedText; // Not encrypted
        }
        
        try {
            String textWithoutPrefix = encryptedText.substring(ENCRYPTION_PREFIX.length());
            return getEncryptor().decrypt(textWithoutPrefix);
        } catch (Exception e) {
            logger.error("Error decrypting text", e);
            return encryptedText; // Return original on error
        }
    }
    
    @Override
    public boolean isEncrypted(String text) {
        return text != null && text.startsWith(ENCRYPTION_PREFIX);
    }
}
