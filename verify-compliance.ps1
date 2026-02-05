# Compliance Verification Script (PowerShell)
# Run this script to verify your backend compliance

Write-Host "🔍 Starting Compliance Verification..." -ForegroundColor Cyan
Write-Host ""

# Counters
$totalChecks = 0
$passedChecks = 0

# Function to check and report
function Check-Compliance {
    param(
        [string]$CheckName,
        [bool]$Result
    )
    
    $script:totalChecks++
    if ($Result) {
        Write-Host "✅ PASS: $CheckName" -ForegroundColor Green
        $script:passedChecks++
        return $true
    } else {
        Write-Host "❌ FAIL: $CheckName" -ForegroundColor Red
        return $false
    }
}

# 1. Check Encryption Algorithm
Write-Host "1. Checking Encryption Configuration..." -ForegroundColor Yellow
$encryptionAlgo = Select-String -Path "src\main\resources\application.properties" -Pattern "PBEWITHHMACSHA512ANDAES_256" -Quiet
Check-Compliance "Encryption Algorithm: AES-256" $encryptionAlgo

$keyIterations = Select-String -Path "src\main\resources\application.properties" -Pattern "key-obtention-iterations=10000" -Quiet
Check-Compliance "Encryption Key Iterations: 10,000" $keyIterations

# 2. Check Database SSL (Production)
Write-Host ""
Write-Host "2. Checking Database SSL Configuration..." -ForegroundColor Yellow
if (Test-Path "src\main\resources\application-production.properties") {
    $dbSSL = Select-String -Path "src\main\resources\application-production.properties" -Pattern "useSSL=true" -Quiet
    Check-Compliance "Database SSL: Enabled (Production)" $dbSSL
} else {
    Write-Host "⚠️  WARN: Production properties file not found" -ForegroundColor Yellow
    $totalChecks++
}

# 3. Check HTTPS Configuration
Write-Host ""
Write-Host "3. Checking HTTPS Configuration..." -ForegroundColor Yellow
if (Test-Path "src\main\resources\application-production.properties") {
    $https = Select-String -Path "src\main\resources\application-production.properties" -Pattern "server.ssl.enabled=true" -Quiet
    Check-Compliance "HTTPS: Enabled (Production)" $https
} else {
    Write-Host "⚠️  WARN: Production properties file not found" -ForegroundColor Yellow
    $totalChecks++
}

# 4. Check CORS Configuration
Write-Host ""
Write-Host "4. Checking CORS Configuration..." -ForegroundColor Yellow
if (Test-Path "src\main\resources\application-production.properties") {
    $corsWildcard = Select-String -Path "src\main\resources\application-production.properties" -Pattern "allowed-origins=\*" -Quiet
    if ($corsWildcard) {
        Write-Host "❌ FAIL: CORS allows all origins in production" -ForegroundColor Red
        $totalChecks++
    } else {
        Check-Compliance "CORS: Restricted (Production)" $true
    }
} else {
    Write-Host "⚠️  WARN: Production properties file not found" -ForegroundColor Yellow
    $totalChecks++
}

# 5. Check Access Control
Write-Host ""
Write-Host "5. Checking Access Control Implementation..." -ForegroundColor Yellow
$rbac = Get-ChildItem -Path "src\main\java\com\medidropbox\controller" -Recurse -Filter "*.java" | 
    Select-String -Pattern "@PreAuthorize" -Quiet
Check-Compliance "Access Control: RBAC Implemented" $rbac

# 6. Check Audit Logging
Write-Host ""
Write-Host "6. Checking Audit Logging..." -ForegroundColor Yellow
$auditService = Get-ChildItem -Path "src\main\java\com\medidropbox\service" -Recurse -Filter "*.java" | 
    Select-String -Pattern "AuditLogService" -Quiet
Check-Compliance "Audit Logging: Service Exists" $auditService

$auditUsage = Get-ChildItem -Path "src\main\java\com\medidropbox\service\impl" -Recurse -Filter "*.java" | 
    Select-String -Pattern "auditLogService.log" -Quiet
Check-Compliance "Audit Logging: Used in Services" $auditUsage

# 7. Check GDPR Endpoints
Write-Host ""
Write-Host "7. Checking GDPR Compliance Endpoints..." -ForegroundColor Yellow
$exportEndpoint = Get-ChildItem -Path "src\main\java\com\medidropbox\controller" -Recurse -Filter "*.java" | 
    Select-String -Pattern "/api/v1/patients/data/export" -Quiet
Check-Compliance "GDPR: Data Export Endpoint" $exportEndpoint

$deleteEndpoint = Get-ChildItem -Path "src\main\java\com\medidropbox\controller" -Recurse -Filter "*.java" | 
    Select-String -Pattern "/api/v1/patients/data/delete" -Quiet
Check-Compliance "GDPR: Data Deletion Endpoint" $deleteEndpoint

$portableEndpoint = Get-ChildItem -Path "src\main\java\com\medidropbox\controller" -Recurse -Filter "*.java" | 
    Select-String -Pattern "/api/v1/patients/data/export/portable" -Quiet
Check-Compliance "GDPR: Data Portability Endpoint" $portableEndpoint

# 8. Check Legal Hold System
Write-Host ""
Write-Host "8. Checking Legal Hold System..." -ForegroundColor Yellow
$legalHoldFields = Get-ChildItem -Path "src\main\java\com\medidropbox\entity" -Recurse -Filter "*.java" | 
    Select-String -Pattern "legalHold" -Quiet
Check-Compliance "Legal Hold: Entity Fields" $legalHoldFields

$legalHoldController = Get-ChildItem -Path "src\main\java\com\medidropbox\controller" -Recurse -Filter "*.java" | 
    Select-String -Pattern "LegalHoldController" -Quiet
Check-Compliance "Legal Hold: Controller Exists" $legalHoldController

# 9. Check Archive System
Write-Host ""
Write-Host "9. Checking Archive System..." -ForegroundColor Yellow
$archiveEntity = Get-ChildItem -Path "src\main\java\com\medidropbox\entity" -Recurse -Filter "*.java" | 
    Select-String -Pattern "ArchivedPatient" -Quiet
Check-Compliance "Archive: Entity Exists" $archiveEntity

$archiveService = Get-ChildItem -Path "src\main\java\com\medidropbox\service\impl" -Recurse -Filter "*.java" | 
    Select-String -Pattern "archivePatientData" -Quiet
Check-Compliance "Archive: Service Method Exists" $archiveService

# 10. Check Patient Consent
Write-Host ""
Write-Host "10. Checking Patient Consent Management..." -ForegroundColor Yellow
$permissionEntity = Get-ChildItem -Path "src\main\java\com\medidropbox\entity" -Recurse -Filter "*.java" | 
    Select-String -Pattern "ReportPermission" -Quiet
Check-Compliance "Consent: Permission Entity Exists" $permissionEntity

$grantPermission = Get-ChildItem -Path "src\main\java\com\medidropbox\controller" -Recurse -Filter "*.java" | 
    Select-String -Pattern "grantPermission" -Quiet
Check-Compliance "Consent: Grant Permission Endpoint" $grantPermission

# Calculate Score
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
$score = [math]::Round(($passedChecks / $totalChecks) * 100, 2)
Write-Host "📊 Compliance Score: $score%" -ForegroundColor Green
Write-Host "✅ Passed: $passedChecks / $totalChecks" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan

if ($score -ge 90) {
    Write-Host "✅ COMPLIANT - Ready for Production" -ForegroundColor Green
} elseif ($score -ge 80) {
    Write-Host "⚠️  MOSTLY COMPLIANT - Minor issues to fix" -ForegroundColor Yellow
} else {
    Write-Host "❌ NOT COMPLIANT - Major issues to fix" -ForegroundColor Red
}

Write-Host ""
Write-Host "🔍 Verification Complete!" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host '1. Run OWASP ZAP: docker run -t owasp/zap2docker-stable zap-baseline.py -t http://localhost:8080'
Write-Host '2. Test SSL: https://www.ssllabs.com/ssltest/'
Write-Host '3. Review GDPR checklist: https://gdpr.eu/checklist/'
Write-Host '4. Review HIPAA checklist: https://www.hhs.gov/hipaa/index.html'
