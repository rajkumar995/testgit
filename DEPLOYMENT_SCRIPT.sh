#!/bin/bash

# =======================================================
# MediDropBox Production Deployment Script
# =======================================================
# This script helps configure and deploy the application
# =======================================================

set -e  # Exit on error

echo "🔒 MediDropBox Production Deployment Script"
echo "============================================"
echo ""

# =======================================================
# Step 1: Generate Encryption Password
# =======================================================
echo "Step 1: Generate Encryption Password"
echo "-------------------------------------"
if [ -z "$ENCRYPTION_PASSWORD" ]; then
    echo "⚠️  ENCRYPTION_PASSWORD not set. Generating..."
    ENCRYPTION_PASSWORD=$(openssl rand -base64 32)
    echo "✅ Generated password: $ENCRYPTION_PASSWORD"
    echo "⚠️  IMPORTANT: Save this password securely!"
    echo "   Add to your environment:"
    echo "   export ENCRYPTION_PASSWORD=\"$ENCRYPTION_PASSWORD\""
    read -p "Press Enter to continue..."
else
    echo "✅ ENCRYPTION_PASSWORD is set"
fi
echo ""

# =======================================================
# Step 2: Configure CORS Origins
# =======================================================
echo "Step 2: Configure CORS Origins"
echo "-------------------------------"
if [ -z "$ALLOWED_ORIGINS" ]; then
    echo "⚠️  ALLOWED_ORIGINS not set"
    echo "Enter allowed origins (comma-separated):"
    echo "Example: https://app.medidropbox.com,https://admin.medidropbox.com"
    read -p "ALLOWED_ORIGINS: " ALLOWED_ORIGINS
    export ALLOWED_ORIGINS="$ALLOWED_ORIGINS"
    echo "✅ Set ALLOWED_ORIGINS=$ALLOWED_ORIGINS"
else
    echo "✅ ALLOWED_ORIGINS is set: $ALLOWED_ORIGINS"
fi
echo ""

# =======================================================
# Step 3: SSL Certificate Setup
# =======================================================
echo "Step 3: SSL Certificate Setup"
echo "------------------------------"
echo "Choose SSL certificate option:"
echo "1. Let's Encrypt (Free, Recommended)"
echo "2. Commercial Certificate"
echo "3. AWS Certificate Manager (ALB)"
echo "4. Skip (configure manually)"
read -p "Enter choice (1-4): " ssl_choice

case $ssl_choice in
    1)
        echo "Setting up Let's Encrypt..."
        read -p "Enter domain name (e.g., api.medidropbox.com): " domain_name
        echo "Installing Certbot..."
        sudo apt-get update
        sudo apt-get install -y certbot
        echo "Obtaining certificate..."
        sudo certbot certonly --standalone -d "$domain_name"
        echo "Converting to PKCS12..."
        read -p "Enter keystore password: " keystore_password
        sudo openssl pkcs12 -export \
            -in /etc/letsencrypt/live/$domain_name/fullchain.pem \
            -inkey /etc/letsencrypt/live/$domain_name/privkey.pem \
            -out /etc/ssl/medidropbox/keystore.p12 \
            -name medidropbox \
            -password pass:$keystore_password
        export SSL_KEYSTORE_PATH="/etc/ssl/medidropbox/keystore.p12"
        export SSL_KEYSTORE_PASSWORD="$keystore_password"
        echo "✅ Certificate configured"
        ;;
    2)
        echo "Commercial certificate setup..."
        read -p "Enter path to certificate file (.crt): " cert_file
        read -p "Enter path to private key file (.key): " key_file
        read -p "Enter keystore password: " keystore_password
        read -p "Enter output path for keystore: " keystore_path
        openssl pkcs12 -export \
            -in "$cert_file" \
            -inkey "$key_file" \
            -out "$keystore_path" \
            -name medidropbox \
            -password pass:$keystore_password
        export SSL_KEYSTORE_PATH="$keystore_path"
        export SSL_KEYSTORE_PASSWORD="$keystore_password"
        echo "✅ Certificate configured"
        ;;
    3)
        echo "AWS Certificate Manager setup..."
        echo "Configure SSL termination at ALB level"
        echo "Backend can use HTTP (ALB handles HTTPS)"
        export SSL_ENABLED="false"
        ;;
    4)
        echo "Skipping SSL setup. Configure manually."
        ;;
    *)
        echo "Invalid choice. Skipping SSL setup."
        ;;
esac
echo ""

# =======================================================
# Step 4: AWS Credentials
# =======================================================
echo "Step 4: AWS Credentials"
echo "-----------------------"
if [ -z "$AWS_ACCESS_KEY_ID" ] || [ -z "$AWS_SECRET_ACCESS_KEY" ]; then
    echo "⚠️  AWS credentials not set"
    read -p "Enter AWS Access Key ID: " aws_key
    read -p "Enter AWS Secret Access Key: " aws_secret
    export AWS_ACCESS_KEY_ID="$aws_key"
    export AWS_SECRET_ACCESS_KEY="$aws_secret"
    echo "✅ AWS credentials set"
else
    echo "✅ AWS credentials are set"
fi
echo ""

# =======================================================
# Step 5: Verify Configuration
# =======================================================
echo "Step 5: Verify Configuration"
echo "-----------------------------"
echo "Configuration Summary:"
echo "  ENCRYPTION_PASSWORD: ${ENCRYPTION_PASSWORD:+SET}"
echo "  ALLOWED_ORIGINS: $ALLOWED_ORIGINS"
echo "  SSL_KEYSTORE_PATH: ${SSL_KEYSTORE_PATH:-Not set}"
echo "  SSL_KEYSTORE_PASSWORD: ${SSL_KEYSTORE_PASSWORD:+SET}"
echo "  AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID:+SET}"
echo "  AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY:+SET}"
echo ""

# =======================================================
# Step 6: Create Environment File
# =======================================================
echo "Step 6: Create Environment File"
echo "---------------------------------"
env_file=".env.production"
cat > "$env_file" << EOF
# MediDropBox Production Environment Variables
# Generated on: $(date)

# Encryption
ENCRYPTION_PASSWORD=$ENCRYPTION_PASSWORD

# CORS
ALLOWED_ORIGINS=$ALLOWED_ORIGINS

# SSL
SSL_ENABLED=true
SSL_KEYSTORE_PATH=${SSL_KEYSTORE_PATH:-/etc/ssl/medidropbox/keystore.p12}
SSL_KEYSTORE_PASSWORD=$SSL_KEYSTORE_PASSWORD
SSL_KEY_ALIAS=medidropbox
SERVER_PORT=8443

# AWS
AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
AWS_S3_BUCKET=medidropbox-reports
AWS_S3_REGION=ap-south-1

# Spring Profile
SPRING_PROFILES_ACTIVE=production
EOF

echo "✅ Environment file created: $env_file"
echo "⚠️  IMPORTANT: Secure this file! Add to .gitignore"
echo ""

# =======================================================
# Step 7: Start Application
# =======================================================
echo "Step 7: Start Application"
echo "-------------------------"
read -p "Start application now? (y/n): " start_app

if [ "$start_app" = "y" ]; then
    echo "Starting application with production profile..."
    source "$env_file"
    java -jar medidropbox.jar --spring.profiles.active=production
else
    echo "To start application later, run:"
    echo "  source $env_file"
    echo "  java -jar medidropbox.jar --spring.profiles.active=production"
fi

echo ""
echo "✅ Deployment script completed!"
echo "📋 Review the configuration and verify all settings are correct."
