# 🔧 Lab Report System - Setup Instructions

## 📋 Prerequisites

1. **AWS S3 Bucket**: Create an S3 bucket named `medidropbox-reports` (or update in `application.properties`)
2. **AWS Credentials**: Get AWS Access Key ID and Secret Access Key
3. **Encryption Password**: Set a strong encryption password for production

## ⚙️ Configuration

### 1. Update `application.properties`

```properties
# AWS S3 Configuration
aws.s3.bucket-name=medidropbox-reports
aws.s3.region=ap-south-1
aws.s3.access-key=${AWS_ACCESS_KEY_ID:}
aws.s3.secret-key=${AWS_SECRET_ACCESS_KEY:}

# Encryption
jasypt.encryptor.password=${ENCRYPTION_PASSWORD:your-strong-password-here}
```

### 2. Set Environment Variables

**For Development:**
- Set `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` in your IDE/terminal
- Set `ENCRYPTION_PASSWORD` (use a strong password)

**For Production:**
- Use environment variables or secure vault
- Never commit credentials to git

## 🗄️ Database

The system will auto-create tables on startup:
- `md_lab_reports` - Stores lab reports
- `md_report_permissions` - Stores patient permissions

## 🧪 Testing

### Patient Flow:
1. Patient uploads report via app
2. File uploaded to S3
3. Report saved with encrypted AI summary/notes
4. Patient can view all reports
5. Patient grants permission to doctor/hospital
6. Doctor can view patient reports (with permission)

### Doctor Flow:
1. Doctor enters patient ID
2. System checks permission
3. If granted, shows accessible reports
4. Doctor can filter by report type

## 📱 Flutter App Features

- ✅ Upload reports (file picker)
- ✅ View all reports
- ✅ Filter by type
- ✅ View report details
- ✅ Grant/revoke permissions
- ✅ View granted permissions

## 🔐 Security

- ✅ Files encrypted at rest in S3
- ✅ Sensitive data (AI summary, notes) encrypted in database
- ✅ Permission-based access control
- ✅ Patient owns all their reports

---

**Ready to use!** Just configure AWS credentials and encryption password.
