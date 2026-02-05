# =======================================================
# MediDropBox Production Deployment Script (PowerShell)
# =======================================================
# This script helps configure and deploy the application on Windows
# =======================================================

Write-Host "🔒 MediDropBox Production Deployment Script" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# =======================================================
# Step 1: Generate Encryption Password
# =======================================================
Write-Host "Step 1: Generate Encryption Password" -ForegroundColor Yellow
Write-Host "-------------------------------------" -ForegroundColor Yellow

if (-not $env:ENCRYPTION_PASSWORD) {
    Write-Host "⚠️  ENCRYPTION_PASSWORD not set. Generating..." -ForegroundColor Yellow
    $bytes = New-Object byte[] 32
    [System.Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes)
    $env:ENCRYPTION_PASSWORD = [Convert]::ToBase64String($bytes)
    Write-Host "✅ Generated password: $($env:ENCRYPTION_PASSWORD)" -ForegroundColor Green
    Write-Host "⚠️  IMPORTANT: Save this password securely!" -ForegroundColor Red
    Write-Host "   Add to your environment:" -ForegroundColor Yellow
    Write-Host "   `$env:ENCRYPTION_PASSWORD=`"$($env:ENCRYPTION_PASSWORD)`"" -ForegroundColor Cyan
    Read-Host "Press Enter to continue"
} else {
    Write-Host "✅ ENCRYPTION_PASSWORD is set" -ForegroundColor Green
}
Write-Host ""

# =======================================================
# Step 2: Configure CORS Origins
# =======================================================
Write-Host "Step 2: Configure CORS Origins" -ForegroundColor Yellow
Write-Host "-------------------------------" -ForegroundColor Yellow

if (-not $env:ALLOWED_ORIGINS) {
    Write-Host "⚠️  ALLOWED_ORIGINS not set" -ForegroundColor Yellow
    Write-Host "Enter allowed origins (comma-separated):" -ForegroundColor Cyan
    Write-Host "Example: https://app.medidropbox.com,https://admin.medidropbox.com" -ForegroundColor Gray
    $origins = Read-Host "ALLOWED_ORIGINS"
    $env:ALLOWED_ORIGINS = $origins
    Write-Host "✅ Set ALLOWED_ORIGINS=$($env:ALLOWED_ORIGINS)" -ForegroundColor Green
} else {
    Write-Host "✅ ALLOWED_ORIGINS is set: $($env:ALLOWED_ORIGINS)" -ForegroundColor Green
}
Write-Host ""

# =======================================================
# Step 3: SSL Certificate Setup
# =======================================================
Write-Host "Step 3: SSL Certificate Setup" -ForegroundColor Yellow
Write-Host "------------------------------" -ForegroundColor Yellow
Write-Host "Choose SSL certificate option:"
Write-Host "1. Let's Encrypt (Free, Recommended - requires WSL or Linux)"
Write-Host "2. Commercial Certificate"
Write-Host "3. AWS Certificate Manager (ALB)"
Write-Host "4. Skip (configure manually)"
$sslChoice = Read-Host "Enter choice (1-4)"

switch ($sslChoice) {
    "1" {
        Write-Host "Let's Encrypt setup..." -ForegroundColor Cyan
        Write-Host "Note: Let's Encrypt requires Linux/WSL. Use option 2 for Windows." -ForegroundColor Yellow
    }
    "2" {
        Write-Host "Commercial certificate setup..." -ForegroundColor Cyan
        $certFile = Read-Host "Enter path to certificate file (.crt)"
        $keyFile = Read-Host "Enter path to private key file (.key)"
        $keystorePassword = Read-Host "Enter keystore password" -AsSecureString
        $keystorePath = Read-Host "Enter output path for keystore"
        
        # Convert to PKCS12 using OpenSSL (requires OpenSSL installed)
        $keystorePasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
            [Runtime.InteropServices.Marshal]::SecureStringToBSTR($keystorePassword)
        )
        
        Write-Host "Converting certificate to PKCS12..." -ForegroundColor Cyan
        Write-Host "Run this command (requires OpenSSL):" -ForegroundColor Yellow
        Write-Host "openssl pkcs12 -export -in `"$certFile`" -inkey `"$keyFile`" -out `"$keystorePath`" -name medidropbox -password pass:$keystorePasswordPlain" -ForegroundColor Cyan
        
        $env:SSL_KEYSTORE_PATH = $keystorePath
        $env:SSL_KEYSTORE_PASSWORD = $keystorePasswordPlain
        Write-Host "✅ Certificate path configured" -ForegroundColor Green
    }
    "3" {
        Write-Host "AWS Certificate Manager setup..." -ForegroundColor Cyan
        Write-Host "Configure SSL termination at ALB level" -ForegroundColor Yellow
        Write-Host "Backend can use HTTP (ALB handles HTTPS)" -ForegroundColor Yellow
        $env:SSL_ENABLED = "false"
    }
    "4" {
        Write-Host "Skipping SSL setup. Configure manually." -ForegroundColor Yellow
    }
    default {
        Write-Host "Invalid choice. Skipping SSL setup." -ForegroundColor Red
    }
}
Write-Host ""

# =======================================================
# Step 4: AWS Credentials
# =======================================================
Write-Host "Step 4: AWS Credentials" -ForegroundColor Yellow
Write-Host "-----------------------" -ForegroundColor Yellow

if (-not $env:AWS_ACCESS_KEY_ID -or -not $env:AWS_SECRET_ACCESS_KEY) {
    Write-Host "⚠️  AWS credentials not set" -ForegroundColor Yellow
    $awsKey = Read-Host "Enter AWS Access Key ID"
    $awsSecret = Read-Host "Enter AWS Secret Access Key" -AsSecureString
    $awsSecretPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($awsSecret)
    )
    $env:AWS_ACCESS_KEY_ID = $awsKey
    $env:AWS_SECRET_ACCESS_KEY = $awsSecretPlain
    Write-Host "✅ AWS credentials set" -ForegroundColor Green
} else {
    Write-Host "✅ AWS credentials are set" -ForegroundColor Green
}
Write-Host ""

# =======================================================
# Step 5: Verify Configuration
# =======================================================
Write-Host "Step 5: Verify Configuration" -ForegroundColor Yellow
Write-Host "-----------------------------" -ForegroundColor Yellow
Write-Host "Configuration Summary:" -ForegroundColor Cyan
Write-Host "  ENCRYPTION_PASSWORD: $(if ($env:ENCRYPTION_PASSWORD) { 'SET' } else { 'NOT SET' })"
Write-Host "  ALLOWED_ORIGINS: $($env:ALLOWED_ORIGINS)"
Write-Host "  SSL_KEYSTORE_PATH: $(if ($env:SSL_KEYSTORE_PATH) { $env:SSL_KEYSTORE_PATH } else { 'Not set' })"
Write-Host "  SSL_KEYSTORE_PASSWORD: $(if ($env:SSL_KEYSTORE_PASSWORD) { 'SET' } else { 'NOT SET' })"
Write-Host "  AWS_ACCESS_KEY_ID: $(if ($env:AWS_ACCESS_KEY_ID) { 'SET' } else { 'NOT SET' })"
Write-Host "  AWS_SECRET_ACCESS_KEY: $(if ($env:AWS_SECRET_ACCESS_KEY) { 'SET' } else { 'NOT SET' })"
Write-Host ""

# =======================================================
# Step 6: Create Environment File
# =======================================================
Write-Host "Step 6: Create Environment File" -ForegroundColor Yellow
Write-Host "---------------------------------" -ForegroundColor Yellow

$envFile = ".env.production"
$content = @"
# MediDropBox Production Environment Variables
# Generated on: $(Get-Date)

# Encryption
ENCRYPTION_PASSWORD=$($env:ENCRYPTION_PASSWORD)

# CORS
ALLOWED_ORIGINS=$($env:ALLOWED_ORIGINS)

# SSL
SSL_ENABLED=true
SSL_KEYSTORE_PATH=$($env:SSL_KEYSTORE_PATH)
SSL_KEYSTORE_PASSWORD=$($env:SSL_KEYSTORE_PASSWORD)
SSL_KEY_ALIAS=medidropbox
SERVER_PORT=8443

# AWS
AWS_ACCESS_KEY_ID=$($env:AWS_ACCESS_KEY_ID)
AWS_SECRET_ACCESS_KEY=$($env:AWS_SECRET_ACCESS_KEY)
AWS_S3_BUCKET=medidropbox-reports
AWS_S3_REGION=ap-south-1

# Spring Profile
SPRING_PROFILES_ACTIVE=production
"@

$content | Out-File -FilePath $envFile -Encoding UTF8
Write-Host "✅ Environment file created: $envFile" -ForegroundColor Green
Write-Host "⚠️  IMPORTANT: Secure this file! Add to .gitignore" -ForegroundColor Red
Write-Host ""

# =======================================================
# Step 7: Start Application
# =======================================================
Write-Host "Step 7: Start Application" -ForegroundColor Yellow
Write-Host "-------------------------" -ForegroundColor Yellow
$startApp = Read-Host "Start application now? (y/n)"

if ($startApp -eq "y") {
    Write-Host "Starting application with production profile..." -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
    java -jar medidropbox.jar --spring.profiles.active=production
} else {
    Write-Host "To start application later, run:" -ForegroundColor Cyan
    Write-Host "  Get-Content $envFile | ForEach-Object { if (`$_ -match '^([^#][^=]+)=(.*)$') { `$name = `$matches[1].Trim(); `$value = `$matches[2].Trim(); [Environment]::SetEnvironmentVariable(`$name, `$value, 'Process') } }" -ForegroundColor Gray
    Write-Host "  java -jar medidropbox.jar --spring.profiles.active=production" -ForegroundColor Gray
}

Write-Host ""
Write-Host "✅ Deployment script completed!" -ForegroundColor Green
Write-Host "📋 Review the configuration and verify all settings are correct." -ForegroundColor Cyan
