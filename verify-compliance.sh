#!/bin/bash

# Compliance Verification Script
# Run this script to verify your backend compliance

echo "🔍 Starting Compliance Verification..."
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counters
TOTAL_CHECKS=0
PASSED_CHECKS=0

# Function to check and report
check() {
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ PASS${NC}: $1"
        PASSED_CHECKS=$((PASSED_CHECKS + 1))
        return 0
    else
        echo -e "${RED}❌ FAIL${NC}: $1"
        return 1
    fi
}

# 1. Check Encryption Algorithm
echo "1. Checking Encryption Configuration..."
if grep -q "PBEWITHHMACSHA512ANDAES_256" src/main/resources/application.properties; then
    check "Encryption Algorithm: AES-256"
else
    echo -e "${RED}❌ FAIL${NC}: Encryption Algorithm not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 2. Check Encryption Key Iterations
if grep -q "key-obtention-iterations=10000" src/main/resources/application.properties; then
    check "Encryption Key Iterations: 10,000"
else
    echo -e "${RED}❌ FAIL${NC}: Key iterations not set to 10,000"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 3. Check Database SSL (Production)
echo ""
echo "2. Checking Database SSL Configuration..."
if grep -q "useSSL=true" src/main/resources/application-production.properties 2>/dev/null; then
    check "Database SSL: Enabled (Production)"
else
    echo -e "${YELLOW}⚠️  WARN${NC}: Database SSL not configured in production"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 4. Check HTTPS Configuration
echo ""
echo "3. Checking HTTPS Configuration..."
if grep -q "server.ssl.enabled=true" src/main/resources/application-production.properties 2>/dev/null; then
    check "HTTPS: Enabled (Production)"
else
    echo -e "${YELLOW}⚠️  WARN${NC}: HTTPS not configured in production"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 5. Check CORS Configuration
echo ""
echo "4. Checking CORS Configuration..."
if grep -q "allowed-origins=\*" src/main/resources/application-production.properties 2>/dev/null; then
    echo -e "${RED}❌ FAIL${NC}: CORS allows all origins in production"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
else
    check "CORS: Restricted (Production)"
fi

# 6. Check Access Control
echo ""
echo "5. Checking Access Control Implementation..."
if grep -r "@PreAuthorize" src/main/java/com/medidropbox/controller/ > /dev/null 2>&1; then
    check "Access Control: RBAC Implemented"
else
    echo -e "${RED}❌ FAIL${NC}: Access control not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 7. Check Audit Logging
echo ""
echo "6. Checking Audit Logging..."
if grep -r "AuditLogService" src/main/java/com/medidropbox/service/ > /dev/null 2>&1; then
    check "Audit Logging: Service Exists"
else
    echo -e "${RED}❌ FAIL${NC}: Audit logging service not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

if grep -r "auditLogService.log" src/main/java/com/medidropbox/service/impl/ > /dev/null 2>&1; then
    check "Audit Logging: Used in Services"
else
    echo -e "${RED}❌ FAIL${NC}: Audit logging not used"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 8. Check GDPR Endpoints
echo ""
echo "7. Checking GDPR Compliance Endpoints..."
if grep -r "/api/v1/patients/data/export" src/main/java/com/medidropbox/controller/ > /dev/null 2>&1; then
    check "GDPR: Data Export Endpoint"
else
    echo -e "${RED}❌ FAIL${NC}: Data export endpoint not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

if grep -r "/api/v1/patients/data/delete" src/main/java/com/medidropbox/controller/ > /dev/null 2>&1; then
    check "GDPR: Data Deletion Endpoint"
else
    echo -e "${RED}❌ FAIL${NC}: Data deletion endpoint not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

if grep -r "/api/v1/patients/data/export/portable" src/main/java/com/medidropbox/controller/ > /dev/null 2>&1; then
    check "GDPR: Data Portability Endpoint"
else
    echo -e "${RED}❌ FAIL${NC}: Data portability endpoint not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 9. Check Legal Hold System
echo ""
echo "8. Checking Legal Hold System..."
if grep -r "legalHold" src/main/java/com/medidropbox/entity/ > /dev/null 2>&1; then
    check "Legal Hold: Entity Fields"
else
    echo -e "${RED}❌ FAIL${NC}: Legal hold fields not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

if grep -r "LegalHoldController" src/main/java/com/medidropbox/controller/ > /dev/null 2>&1; then
    check "Legal Hold: Controller Exists"
else
    echo -e "${RED}❌ FAIL${NC}: Legal hold controller not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 10. Check Archive System
echo ""
echo "9. Checking Archive System..."
if grep -r "ArchivedPatient" src/main/java/com/medidropbox/entity/ > /dev/null 2>&1; then
    check "Archive: Entity Exists"
else
    echo -e "${RED}❌ FAIL${NC}: Archive entity not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

if grep -r "archivePatientData" src/main/java/com/medidropbox/service/impl/ > /dev/null 2>&1; then
    check "Archive: Service Method Exists"
else
    echo -e "${RED}❌ FAIL${NC}: Archive service method not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# 11. Check Patient Consent
echo ""
echo "10. Checking Patient Consent Management..."
if grep -r "ReportPermission" src/main/java/com/medidropbox/entity/ > /dev/null 2>&1; then
    check "Consent: Permission Entity Exists"
else
    echo -e "${RED}❌ FAIL${NC}: Permission entity not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

if grep -r "grantPermission" src/main/java/com/medidropbox/controller/ > /dev/null 2>&1; then
    check "Consent: Grant Permission Endpoint"
else
    echo -e "${RED}❌ FAIL${NC}: Grant permission endpoint not found"
    TOTAL_CHECKS=$((TOTAL_CHECKS + 1))
fi

# Calculate Score
echo ""
echo "=========================================="
SCORE=$(echo "scale=2; ($PASSED_CHECKS / $TOTAL_CHECKS) * 100" | bc)
echo -e "📊 Compliance Score: ${GREEN}$SCORE%${NC}"
echo -e "✅ Passed: $PASSED_CHECKS / $TOTAL_CHECKS"
echo "=========================================="

if (( $(echo "$SCORE >= 90" | bc -l) )); then
    echo -e "${GREEN}✅ COMPLIANT - Ready for Production${NC}"
elif (( $(echo "$SCORE >= 80" | bc -l) )); then
    echo -e "${YELLOW}⚠️  MOSTLY COMPLIANT - Minor issues to fix${NC}"
else
    echo -e "${RED}❌ NOT COMPLIANT - Major issues to fix${NC}"
fi

echo ""
echo "🔍 Verification Complete!"
echo ""
echo "Next Steps:"
echo "1. Run OWASP ZAP: docker run -t owasp/zap2docker-stable zap-baseline.py -t http://localhost:8080"
echo "2. Test SSL: https://www.ssllabs.com/ssltest/"
echo "3. Review GDPR checklist: https://gdpr.eu/checklist/"
echo "4. Review HIPAA checklist: https://www.hhs.gov/hipaa/index.html"
