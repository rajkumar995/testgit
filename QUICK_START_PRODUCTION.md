# 🚀 Quick Start: Production Configuration

## ⚡ Fast Setup (5 Minutes)

### **1. Generate Encryption Password**
```bash
# Linux/Mac
openssl rand -base64 32

# Windows PowerShell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | % {[char]$_})
```

**Save the generated password securely!**

### **2. Set Environment Variables**
```bash
# Linux/Mac
export ENCRYPTION_PASSWORD="paste-generated-password-here"
export ALLOWED_ORIGINS="https://app.medidropbox.com,https://admin.medidropbox.com"
export SSL_KEYSTORE_PASSWORD="your-keystore-password"
export AWS_ACCESS_KEY_ID="your-aws-key"
export AWS_SECRET_ACCESS_KEY="your-aws-secret"

# Windows PowerShell
$env:ENCRYPTION_PASSWORD="paste-generated-password-here"
$env:ALLOWED_ORIGINS="https://app.medidropbox.com,https://admin.medidropbox.com"
$env:SSL_KEYSTORE_PASSWORD="your-keystore-password"
$env:AWS_ACCESS_KEY_ID="your-aws-key"
$env:AWS_SECRET_ACCESS_KEY="your-aws-secret"
```

### **3. Get SSL Certificate**

#### **Option A: Let's Encrypt (Free)**
```bash
# Install Certbot
sudo apt-get install certbot

# Get certificate
sudo certbot certonly --standalone -d api.medidropbox.com

# Convert to PKCS12
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/api.medidropbox.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/api.medidropbox.com/privkey.pem \
  -out /etc/ssl/medidropbox/keystore.p12 \
  -name medidropbox \
  -password pass:YOUR_KEYSTORE_PASSWORD
```

#### **Option B: Commercial Certificate**
```bash
# Convert purchased certificate to PKCS12
openssl pkcs12 -export \
  -in certificate.crt \
  -inkey private.key \
  -out keystore.p12 \
  -name medidropbox \
  -password pass:YOUR_KEYSTORE_PASSWORD
```

### **4. Update application.properties**
```properties
# Set keystore path
server.ssl.key-store=/path/to/keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
```

### **5. Start Application**
```bash
# With production profile
java -jar medidropbox.jar --spring.profiles.active=production

# Or set profile in environment
export SPRING_PROFILES_ACTIVE=production
java -jar medidropbox.jar
```

---

## ✅ Verification

### **Test HTTPS:**
```bash
curl https://api.medidropbox.com/api/v1/hospitals
```

### **Test CORS:**
```javascript
// From browser console on allowed domain
fetch('https://api.medidropbox.com/api/v1/patients/me', {
  headers: { 'Authorization': 'Bearer TOKEN' }
})
```

### **Check Audit Logs:**
```bash
tail -f logs/audit.log
```

---

## 📋 Checklist

- [ ] Encryption password generated and set
- [ ] CORS origins configured
- [ ] SSL certificate obtained and configured
- [ ] AWS credentials set
- [ ] Application starts with production profile
- [ ] HTTPS works (no certificate errors)
- [ ] CORS works (no CORS errors from allowed domains)
- [ ] Audit logs are being written

---

**That's it! Your application is now production-ready and secure.** ✅
