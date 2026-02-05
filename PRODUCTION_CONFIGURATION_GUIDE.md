# 🔧 Production Configuration Guide

## 1. Encryption Password (Environment Variable)

### **Why It's Critical:**
- Used to encrypt/decrypt sensitive data (AI summary, notes)
- Must be strong and kept secret
- Changing it will make existing encrypted data unreadable

### **How to Set:**

#### **Option 1: Environment Variable (Recommended)**
```bash
# Linux/Mac
export ENCRYPTION_PASSWORD="your-very-strong-password-minimum-32-characters-long"

# Windows PowerShell
$env:ENCRYPTION_PASSWORD="your-very-strong-password-minimum-32-characters-long"

# Windows CMD
set ENCRYPTION_PASSWORD=your-very-strong-password-minimum-32-characters-long
```

#### **Option 2: application.properties (NOT RECOMMENDED for production)**
```properties
# ⚠️ Only for development - use environment variable in production
jasypt.encryptor.password=${ENCRYPTION_PASSWORD:default-dev-password}
```

#### **Option 3: Docker/Container**
```yaml
# docker-compose.yml
environment:
  - ENCRYPTION_PASSWORD=your-very-strong-password-minimum-32-characters-long
```

#### **Option 4: System Properties**
```bash
java -jar app.jar -DENCRYPTION_PASSWORD=your-very-strong-password-minimum-32-characters-long
```

### **Password Requirements:**
- ✅ Minimum 32 characters
- ✅ Mix of uppercase, lowercase, numbers, symbols
- ✅ Randomly generated (use password generator)
- ✅ Stored securely (never commit to git)
- ✅ Rotated periodically (requires re-encryption of data)

### **Generate Strong Password:**
```bash
# Linux/Mac
openssl rand -base64 32

# Or use online password generator (minimum 32 chars)
```

### **Example:**
```bash
ENCRYPTION_PASSWORD="K8#mP2$vL9@nQ4&wR7!tY5*uI3^oE6%aS1"
```

---

## 2. CORS Origins (Restrict to Specific Domains)

### **Why It's Critical:**
- Prevents unauthorized domains from accessing your API
- Security best practice
- Required for production

### **Current Configuration:**
```properties
# Development (allows all origins - NOT for production)
spring.web.cors.allowed-origins=*
```

### **Production Configuration:**

#### **Option 1: Environment Variable (Recommended)**
```bash
# Linux/Mac
export ALLOWED_ORIGINS="https://app.medidropbox.com,https://admin.medidropbox.com,https://www.medidropbox.com"

# Windows PowerShell
$env:ALLOWED_ORIGINS="https://app.medidropbox.com,https://admin.medidropbox.com,https://www.medidropbox.com"
```

#### **Option 2: application.properties**
```properties
# Production: Specific domains only
spring.web.cors.allowed-origins=${ALLOWED_ORIGINS:https://app.medidropbox.com,https://admin.medidropbox.com}
```

#### **Option 3: application-production.properties**
```properties
# Create: src/main/resources/application-production.properties
spring.web.cors.allowed-origins=https://app.medidropbox.com,https://admin.medidropbox.com,https://www.medidropbox.com
```

### **Format:**
- Separate multiple domains with commas
- Include protocol (https://)
- No trailing slashes
- Include all subdomains if needed

### **Examples:**
```properties
# Single domain
ALLOWED_ORIGINS=https://app.medidropbox.com

# Multiple domains
ALLOWED_ORIGINS=https://app.medidropbox.com,https://admin.medidropbox.com,https://api.medidropbox.com

# Include localhost for testing (remove in production)
ALLOWED_ORIGINS=https://app.medidropbox.com,http://localhost:3000
```

### **Testing:**
```bash
# Test CORS from browser console
fetch('https://api.medidropbox.com/api/v1/patients/me', {
  headers: { 'Authorization': 'Bearer YOUR_TOKEN' }
})
.then(r => r.json())
.then(console.log)
```

---

## 3. HTTPS Certificate

### **Why It's Critical:**
- Encrypts all data in transit
- Required for compliance (GDPR, HIPAA)
- Builds user trust
- Prevents man-in-the-middle attacks

### **Option 1: Let's Encrypt (Free, Recommended)**

#### **Step 1: Install Certbot**
```bash
# Ubuntu/Debian
sudo apt-get update
sudo apt-get install certbot

# CentOS/RHEL
sudo yum install certbot
```

#### **Step 2: Obtain Certificate**
```bash
sudo certbot certonly --standalone -d api.medidropbox.com
```

#### **Step 3: Convert to PKCS12**
```bash
# Convert to PKCS12 format for Java
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/api.medidropbox.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/api.medidropbox.com/privkey.pem \
  -out /path/to/keystore.p12 \
  -name medidropbox \
  -password pass:YOUR_KEYSTORE_PASSWORD
```

#### **Step 4: Configure application.properties**
```properties
server.ssl.enabled=true
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=medidropbox
server.port=8443
```

#### **Step 5: Auto-Renewal (Cron)**
```bash
# Add to crontab
0 0 * * * certbot renew --quiet --deploy-hook "systemctl restart your-app"
```

### **Option 2: Commercial SSL Certificate**

#### **Step 1: Purchase Certificate**
- Buy from: DigiCert, GlobalSign, Sectigo, etc.
- Get: Certificate file (.crt) and Private Key (.key)

#### **Step 2: Convert to PKCS12**
```bash
openssl pkcs12 -export \
  -in certificate.crt \
  -inkey private.key \
  -out keystore.p12 \
  -name medidropbox \
  -password pass:YOUR_KEYSTORE_PASSWORD
```

#### **Step 3: Configure application.properties**
```properties
server.ssl.enabled=true
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=medidropbox
server.port=8443
```

### **Option 3: AWS Certificate Manager (ACM) - For AWS Deployment**

#### **Step 1: Request Certificate in ACM**
- Go to AWS Certificate Manager
- Request public certificate
- Validate domain ownership
- Certificate will be provisioned

#### **Step 2: Use with Load Balancer**
- Attach certificate to Application Load Balancer (ALB)
- Configure ALB to terminate SSL
- Backend can use HTTP (ALB handles HTTPS)

#### **Step 3: application.properties**
```properties
# If using ALB, SSL termination happens at load balancer
server.ssl.enabled=false
server.port=8080
```

### **Option 4: Self-Signed Certificate (Development Only)**

#### **Generate Self-Signed Certificate:**
```bash
keytool -genkeypair \
  -alias medidropbox \
  -keyalg RSA \
  -keysize 2048 \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365 \
  -storepass YOUR_KEYSTORE_PASSWORD
```

#### **Configure:**
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=medidropbox
server.port=8443
```

⚠️ **Warning**: Self-signed certificates are NOT for production. Browsers will show security warnings.

---

## 📋 Complete Production Configuration

### **application-production.properties**
```properties
# =======================================================
# PRODUCTION CONFIGURATION
# =======================================================

# Encryption (from environment variable)
jasypt.encryptor.password=${ENCRYPTION_PASSWORD}
jasypt.encryptor.algorithm=PBEWITHHMACSHA512ANDAES_256
jasypt.encryptor.key-obtention-iterations=10000

# CORS (from environment variable)
spring.web.cors.allowed-origins=${ALLOWED_ORIGINS:https://app.medidropbox.com,https://admin.medidropbox.com}

# HTTPS
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH:/path/to/keystore.p12}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=medidropbox
server.port=8443

# Database SSL (already configured)
spring.datasource.url=jdbc:mysql://...&useSSL=true&requireSSL=true&verifyServerCertificate=true

# Logging
logging.level.root=INFO
logging.level.com.medidropbox=INFO
logging.level.org.hibernate.SQL=WARN
```

### **Environment Variables (Set Before Starting)**
```bash
# Required
export ENCRYPTION_PASSWORD="your-strong-32-char-password"
export ALLOWED_ORIGINS="https://app.medidropbox.com,https://admin.medidropbox.com"
export SSL_KEYSTORE_PASSWORD="your-keystore-password"
export SSL_KEYSTORE_PATH="/path/to/keystore.p12"

# Optional (if not in application.properties)
export AWS_ACCESS_KEY_ID="your-aws-key"
export AWS_SECRET_ACCESS_KEY="your-aws-secret"
```

---

## 🚀 Deployment Steps

### **1. Generate Encryption Password**
```bash
# Generate strong password
openssl rand -base64 32
# Save securely (password manager, vault, etc.)
```

### **2. Set Environment Variables**
```bash
export ENCRYPTION_PASSWORD="generated-password"
export ALLOWED_ORIGINS="https://app.medidropbox.com,https://admin.medidropbox.com"
export SSL_KEYSTORE_PASSWORD="keystore-password"
```

### **3. Obtain SSL Certificate**
```bash
# Option A: Let's Encrypt (free)
sudo certbot certonly --standalone -d api.medidropbox.com

# Option B: Commercial certificate
# Purchase and download certificate files
```

### **4. Convert Certificate to PKCS12**
```bash
openssl pkcs12 -export \
  -in fullchain.pem \
  -inkey privkey.pem \
  -out keystore.p12 \
  -name medidropbox \
  -password pass:YOUR_KEYSTORE_PASSWORD
```

### **5. Configure application.properties**
```properties
# Update paths and passwords
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
```

### **6. Start Application**
```bash
# With production profile
java -jar medidropbox.jar --spring.profiles.active=production

# Or set in environment
export SPRING_PROFILES_ACTIVE=production
java -jar medidropbox.jar
```

---

## ✅ Verification Checklist

### **After Configuration:**

1. ✅ **Test HTTPS**
   ```bash
   curl https://api.medidropbox.com/api/v1/hospitals
   # Should return data (not certificate error)
   ```

2. ✅ **Test CORS**
   ```javascript
   // From browser console on allowed domain
   fetch('https://api.medidropbox.com/api/v1/patients/me', {
     headers: { 'Authorization': 'Bearer TOKEN' }
   })
   // Should work (no CORS error)
   ```

3. ✅ **Test Encryption**
   - Upload lab report with AI summary
   - Check database - should see encrypted value (starts with "ENC:")
   - Retrieve report - should see decrypted value

4. ✅ **Check Audit Logs**
   ```bash
   tail -f logs/audit.log
   # Should see authentication and data access logs
   ```

---

## 🔐 Security Best Practices

1. ✅ **Never commit secrets to git**
2. ✅ **Use environment variables for sensitive data**
3. ✅ **Rotate encryption password periodically**
4. ✅ **Use strong passwords (32+ characters)**
5. ✅ **Store certificates securely**
6. ✅ **Enable certificate auto-renewal**
7. ✅ **Monitor audit logs regularly**
8. ✅ **Restrict CORS to specific domains only**

---

**Status**: ✅ Configuration guide ready. Follow steps above to secure your production deployment.
