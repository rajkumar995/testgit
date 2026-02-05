# 🔒 Production Configuration Guide

## Required Configuration Changes

### 1. Database SSL Configuration

**Current (Development):**
```properties
spring.datasource.url=jdbc:mysql://...?useSSL=false&...
```

**Production (REQUIRED):**
```properties
spring.datasource.url=jdbc:mysql://...?useSSL=true&requireSSL=true&verifyServerCertificate=true&...
```

**Action:** Update `application-production.properties`

---

### 2. Encryption Password

**Current:**
```properties
jasypt.encryptor.password=${ENCRYPTION_PASSWORD:medidropbox-secret-key-change-in-production}
```

**Production (REQUIRED):**
```properties
# Use environment variable - NEVER hardcode
jasypt.encryptor.password=${ENCRYPTION_PASSWORD}
```

**Action:**
1. Generate strong password: `openssl rand -base64 32`
2. Set environment variable: `export ENCRYPTION_PASSWORD=<generated-password>`
3. Update `application-production.properties`

---

### 3. CORS Configuration

**Current (Development):**
```properties
spring.web.cors.allowed-origins=*
```

**Production (REQUIRED):**
```properties
# Replace with your actual frontend domains
spring.web.cors.allowed-origins=https://app.medidropbox.com,https://admin.medidropbox.com
```

**Action:** Update `application-production.properties`

---

### 4. HTTPS Certificate

**Add to `application-production.properties`:**
```properties
# HTTPS Configuration
server.ssl.enabled=true
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=medidropbox

# Redirect HTTP to HTTPS
server.http.port=80
server.port=443
```

**Action:**
1. Obtain SSL certificate (Let's Encrypt, AWS Certificate Manager, etc.)
2. Create keystore
3. Set `SSL_KEYSTORE_PASSWORD` environment variable
4. Update paths in configuration

---

### 5. AWS Credentials

**Current (use env vars – no hardcoded keys):**
```properties
aws.s3.access-key=${aws_access_key}
aws.s3.secret-key=${aws_secret_key}
```

**Production (REQUIRED):**
Set environment variables `aws_access_key` and `aws_secret_key` (or use IAM roles). Never commit real keys to the repo.

**Action:**
1. Create IAM user with minimal S3 permissions
2. Set environment variables
3. Remove hardcoded credentials

---

### 6. Logging Configuration

**Add to `application-production.properties`:**
```properties
# Production Logging
logging.level.root=INFO
logging.level.com.medidropbox=INFO
logging.level.org.springframework=WARN
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Audit Logging
logging.logger.AUDIT_LOG.name=AUDIT_LOG
logging.logger.AUDIT_LOG.level=INFO
logging.logger.AUDIT_LOG.appender-ref=audit-file
```

---

## Environment Variables Required

Create `.env` file or set in deployment platform:

```bash
# Encryption
ENCRYPTION_PASSWORD=<strong-password>

# Database
DB_PASSWORD=<database-password>

# SSL
SSL_KEYSTORE_PASSWORD=<keystore-password>

# AWS (set these or use IAM roles)
aws_access_key=<access-key>
aws_secret_key=<secret-key>

# CORS (comma-separated)
ALLOWED_ORIGINS=https://app.medidropbox.com,https://admin.medidropbox.com
```

---

## Security Checklist

- [ ] Database SSL enabled
- [ ] Strong encryption password set
- [ ] CORS restricted to production domains
- [ ] HTTPS certificate configured
- [ ] AWS credentials in environment variables
- [ ] Audit logging configured
- [ ] Error messages don't expose sensitive data
- [ ] Rate limiting enabled
- [ ] Security headers configured
- [ ] Backup encryption enabled

---

## Deployment Steps

1. **Update Configuration:**
   - Copy `application.properties` to `application-production.properties`
   - Update all production settings

2. **Set Environment Variables:**
   - Encryption password
   - Database password
   - SSL keystore password
   - AWS credentials
   - CORS origins

3. **Obtain SSL Certificate:**
   - Let's Encrypt (free)
   - AWS Certificate Manager
   - Commercial certificate

4. **Test Configuration:**
   - Run in staging environment
   - Test all endpoints
   - Verify SSL/TLS
   - Check CORS

5. **Deploy:**
   - Build with production profile
   - Deploy to production server
   - Monitor logs
   - Verify health checks

---

## Monitoring

After deployment, monitor:
- Application logs
- Audit logs
- Database connections
- SSL certificate expiration
- API response times
- Error rates
