package com.medidropbox.service.impl;

import com.medidropbox.dto.request.HealthProfileRequest;
import com.medidropbox.dto.request.VitalsHistoryRequest;
import com.medidropbox.dto.request.VitalsRecordRequest;
import com.medidropbox.dto.response.BmiReportResponse;
import com.medidropbox.dto.response.HealthProfileResponse;
import com.medidropbox.dto.response.LatestVitalsResponse;
import com.medidropbox.dto.response.VitalsRecordResponse;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.HealthProfile;
import com.medidropbox.entity.VitalsRecord;
import com.medidropbox.enums.VitalType;
import com.medidropbox.repository.GlobalPatientRepository;
import com.medidropbox.repository.HealthPermissionRepository;
import com.medidropbox.repository.HealthProfileRepository;
import com.medidropbox.repository.VitalsPermissionRepository;
import com.medidropbox.repository.VitalsRecordRepository;
import com.medidropbox.service.HealthProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class HealthProfileServiceImpl implements HealthProfileService {
    
    private final HealthProfileRepository healthProfileRepository;
    private final VitalsRecordRepository vitalsRecordRepository;
    private final GlobalPatientRepository globalPatientRepository;
    private final HealthPermissionRepository healthPermissionRepository;
    private final VitalsPermissionRepository vitalsPermissionRepository;

    // Inspirational quotes for BMI categories
    private static final String[] UNDERWEIGHT_QUOTES = {
        "Every journey begins with a single step. You're on the right path to better health!",
        "Your body is your temple. Nourish it well and watch it flourish.",
        "Small changes lead to big results. Keep going!",
        "Health is wealth, and you're investing in yourself. That's amazing!"
    };
    
    private static final String[] NORMAL_QUOTES = {
        "You're doing great! Maintaining a healthy weight is a wonderful achievement.",
        "Your dedication to health is inspiring. Keep up the excellent work!",
        "Balance is the key to wellness, and you've found it. Well done!",
        "A healthy body is a happy body. You're on the right track!"
    };
    
    private static final String[] OVERWEIGHT_QUOTES = {
        "Every step forward counts. Your commitment to health is admirable!",
        "Progress, not perfection. You're making positive changes every day.",
        "Your health journey is unique and valuable. Keep moving forward!",
        "Small consistent actions lead to lasting results. You've got this!"
    };
    
    private static final String[] OBESE_QUOTES = {
        "Your determination to improve your health is powerful. Keep going!",
        "Every positive choice matters. You're building a healthier future.",
        "Transformation takes time, but you're already on the path. Stay strong!",
        "Your health is worth the effort. Every day is a new opportunity!"
    };
    
    public HealthProfileServiceImpl(HealthProfileRepository healthProfileRepository,
                                   VitalsRecordRepository vitalsRecordRepository,
                                   GlobalPatientRepository globalPatientRepository,
                                   HealthPermissionRepository healthPermissionRepository,
                                   VitalsPermissionRepository vitalsPermissionRepository) {
        this.healthProfileRepository = healthProfileRepository;
        this.vitalsRecordRepository = vitalsRecordRepository;
        this.globalPatientRepository = globalPatientRepository;
        this.healthPermissionRepository = healthPermissionRepository;
        this.vitalsPermissionRepository = vitalsPermissionRepository;
    }
    
    @Override
    public HealthProfileResponse createOrUpdateHealthProfile(Long globalPatientId, HealthProfileRequest request) {
        // Verify patient exists
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        // Check if profile exists
        Optional<HealthProfile> existingProfile = healthProfileRepository.findByGlobalPatientId(globalPatientId);
        
        HealthProfile profile;
        if (existingProfile.isPresent()) {
            // Update existing profile (replaces old values)
            profile = existingProfile.get();
        } else {
            // Create new profile
            profile = new HealthProfile();
            profile.setGlobalPatient(patient);
        }
        
        // Update fields
        profile.setHeightCm(request.getHeightCm());
        profile.setBloodGroup(request.getBloodGroup());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        
        profile = healthProfileRepository.save(profile);
        
        return mapToHealthProfileResponse(profile);
    }
    
    @Override
    public HealthProfileResponse getHealthProfile(Long globalPatientId) {
        HealthProfile profile = healthProfileRepository.findByGlobalPatientId(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Health profile not found for patient ID: " + globalPatientId));
        
        return mapToHealthProfileResponse(profile);
    }
    
    @Override
    public VitalsRecordResponse createVitalsRecord(Long globalPatientId, VitalsRecordRequest request) {
        // Validate at least one vital is provided
        if (!request.hasAtLeastOneVital()) {
            throw new RuntimeException("At least one vital value is required");
        }
        
        // Verify patient exists and get patient
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        // Create new vitals record (preserves history)
        VitalsRecord record = new VitalsRecord();
        record.setGlobalPatient(patient);
        record.setWeightKg(request.getWeightKg());
        record.setBloodGlucoseMgdl(request.getBloodGlucoseMgdl());
        record.setBloodPressureSystolic(request.getBloodPressureSystolic());
        record.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        record.setRecordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now());
        record.setNotes(request.getNotes());
        
        record = vitalsRecordRepository.save(record);
        
        return mapToVitalsRecordResponse(record);
    }
    
    @Override
    public VitalsRecordResponse updateVitalsRecord(Long globalPatientId, Long recordId, VitalsRecordRequest request) {
        // Verify patient exists
        globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        // Find record and verify ownership
        VitalsRecord record = vitalsRecordRepository.findByIdAndGlobalPatientId(recordId, globalPatientId)
            .orElseThrow(() -> new RuntimeException("Vitals record not found or you don't have permission to update it"));
        
        // Update fields (only provided fields)
        if (request.getWeightKg() != null) {
            record.setWeightKg(request.getWeightKg());
        }
        if (request.getBloodGlucoseMgdl() != null) {
            record.setBloodGlucoseMgdl(request.getBloodGlucoseMgdl());
        }
        if (request.getBloodPressureSystolic() != null) {
            record.setBloodPressureSystolic(request.getBloodPressureSystolic());
        }
        if (request.getBloodPressureDiastolic() != null) {
            record.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        }
        if (request.getRecordedAt() != null) {
            record.setRecordedAt(request.getRecordedAt());
        }
        if (request.getNotes() != null) {
            record.setNotes(request.getNotes());
        }
        
        record = vitalsRecordRepository.save(record);
        
        return mapToVitalsRecordResponse(record);
    }
    
    @Override
    public void deleteVitalsRecord(Long globalPatientId, Long recordId) {
        // Verify patient exists
        globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        // Find record and verify ownership
        VitalsRecord record = vitalsRecordRepository.findByIdAndGlobalPatientId(recordId, globalPatientId)
            .orElseThrow(() -> new RuntimeException("Vitals record not found or you don't have permission to delete it"));
        
        vitalsRecordRepository.delete(record);
    }
    
    @Override
    public LatestVitalsResponse getLatestVitals(Long globalPatientId) {
        // Verify patient exists
        globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        // Get latest weight
        Optional<VitalsRecord> latestWeight = vitalsRecordRepository.findLatestWeightByPatientId(globalPatientId);
        
        // Get latest blood glucose
        Optional<VitalsRecord> latestGlucose = vitalsRecordRepository.findLatestBloodGlucoseByPatientId(globalPatientId);
        
        // Get latest blood pressure
        Optional<VitalsRecord> latestBP = vitalsRecordRepository.findLatestBloodPressureByPatientId(globalPatientId);
        
        // Get health profile for BMI calculation
        Optional<HealthProfile> healthProfile = healthProfileRepository.findByGlobalPatientId(globalPatientId);
        
        // Calculate BMI
        Double bmi = null;
        String bmiCategory = null;
        String bmiQuote = null;
        
        if (latestWeight.isPresent() && healthProfile.isPresent() && healthProfile.get().getHeightCm() != null) {
            Double weight = latestWeight.get().getWeightKg();
            Double height = healthProfile.get().getHeightCm();
            
            if (weight != null && height != null && height > 0) {
                // Convert height from cm to meters
                double heightInMeters = height / 100.0;
                bmi = weight / (heightInMeters * heightInMeters);
                
                // Round to 2 decimal places
                bmi = BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP).doubleValue();
                
                // Determine category
                bmiCategory = getBmiCategory(bmi);
                bmiQuote = getBmiQuote(bmiCategory);
            }
        }
        
        LatestVitalsResponse response = LatestVitalsResponse.builder()
            .latestWeight(latestWeight.map(VitalsRecord::getWeightKg).orElse(null))
            .latestWeightDate(latestWeight.map(VitalsRecord::getRecordedAt).orElse(null))
            .latestBloodGlucose(latestGlucose.map(VitalsRecord::getBloodGlucoseMgdl).orElse(null))
            .latestBloodGlucoseDate(latestGlucose.map(VitalsRecord::getRecordedAt).orElse(null))
            .latestBloodPressureSystolic(latestBP.map(VitalsRecord::getBloodPressureSystolic).orElse(null))
            .latestBloodPressureDiastolic(latestBP.map(VitalsRecord::getBloodPressureDiastolic).orElse(null))
            .latestBloodPressureDisplay(latestBP.map(bp -> 
                bp.getBloodPressureSystolic() + "/" + bp.getBloodPressureDiastolic()).orElse(null))
            .latestBloodPressureDate(latestBP.map(VitalsRecord::getRecordedAt).orElse(null))
            .bmi(bmi)
            .bmiCategory(bmiCategory)
            .bmiQuote(bmiQuote)
            .disclaimer("This data is patient-entered and is for tracking purposes only. " +
                       "It should not be used as a substitute for professional medical advice, diagnosis, or treatment.")
            .build();
        
        return response;
    }
    
    @Override
    public List<VitalsRecordResponse> getVitalsHistory(Long globalPatientId, VitalsHistoryRequest request) {
        // Verify patient exists
        globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        List<VitalsRecord> records;
        
        if (request.getStartDate() != null || request.getEndDate() != null) {
            // Filter by date range
            records = vitalsRecordRepository.findByPatientIdAndDateRange(
                globalPatientId,
                request.getStartDate(),
                request.getEndDate()
            );
        } else {
            // Get all records
            records = vitalsRecordRepository.findByGlobalPatientIdOrderByRecordedAtDesc(globalPatientId);
        }
        
        // Filter by vital type if specified
        if (request.getVitalType() != null && request.getVitalType() != VitalType.ALL) {
            records = filterByVitalType(records, request.getVitalType());
        }
        
        return records.stream()
            .map(this::mapToVitalsRecordResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public BmiReportResponse getBmiReport(Long globalPatientId) {
        // Verify patient exists
        globalPatientRepository.findById(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        
        // Get health profile
        HealthProfile healthProfile = healthProfileRepository.findByGlobalPatientId(globalPatientId)
            .orElseThrow(() -> new RuntimeException("Health profile not found. Please create your health profile first."));
        
        if (healthProfile.getHeightCm() == null) {
            throw new RuntimeException("Height is required for BMI calculation. Please update your health profile.");
        }
        
        // Get latest weight
        Optional<VitalsRecord> latestWeight = vitalsRecordRepository.findLatestWeightByPatientId(globalPatientId);
        
        if (latestWeight.isEmpty() || latestWeight.get().getWeightKg() == null) {
            throw new RuntimeException("No weight record found. Please record your weight first.");
        }
        
        Double weight = latestWeight.get().getWeightKg();
        Double height = healthProfile.getHeightCm();
        
        // Calculate BMI
        double heightInMeters = height / 100.0;
        double bmi = weight / (heightInMeters * heightInMeters);
        bmi = BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP).doubleValue();
        
        // Get category and details
        String category = getBmiCategory(bmi);
        String description = getBmiDescription(category);
        String quote = getBmiQuote(category);
        String recommendation = getBmiRecommendation(category);
        
        return BmiReportResponse.builder()
            .bmi(bmi)
            .bmiCategory(category)
            .bmiDescription(description)
            .inspirationalQuote(quote)
            .heightCm(height)
            .weightKg(weight)
            .weightRecordedAt(latestWeight.get().getRecordedAt())
            .healthRecommendation(recommendation)
            .disclaimer("This BMI calculation is based on patient-entered data and is for tracking purposes only. " +
                       "BMI is a screening tool and does not diagnose health conditions. " +
                       "Please consult with a healthcare professional for medical advice, diagnosis, or treatment.")
            .build();
    }

    @Override
    public HealthProfileResponse getHealthProfileForHospitalContext(Long globalPatientId, Long hospitalId) {
        globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        boolean hasPermission = healthPermissionRepository.hasPermission(globalPatientId, null, hospitalId);
        if (hasPermission) {
            return getHealthProfile(globalPatientId);
        }
        return healthProfileRepository.findByGlobalPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId)
                .map(this::mapToHealthProfileResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Health profile not found or access denied"));
    }

    @Override
    public HealthProfileResponse createOrUpdateHealthProfileForHospital(Long globalPatientId, Long hospitalId, Long doctorId, HealthProfileRequest request) {
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        Optional<HealthProfile> existingProfile = healthProfileRepository.findByGlobalPatientId(globalPatientId);
        HealthProfile profile;
        if (existingProfile.isPresent()) {
            profile = existingProfile.get();
        } else {
            profile = new HealthProfile();
            profile.setGlobalPatient(patient);
        }
        profile.setHeightCm(request.getHeightCm());
        profile.setBloodGroup(request.getBloodGroup());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setUploadedByHospitalId(hospitalId);
        profile.setUploadedByDoctorId(doctorId);
        profile = healthProfileRepository.save(profile);
        return mapToHealthProfileResponse(profile);
    }

    @Override
    public LatestVitalsResponse getLatestVitalsForHospitalContext(Long globalPatientId, Long hospitalId) {
        globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        boolean hasPermission = vitalsPermissionRepository.hasPermission(globalPatientId, null, hospitalId);
        if (hasPermission) {
            return getLatestVitals(globalPatientId);
        }
        Optional<VitalsRecord> latestWeight = vitalsRecordRepository.findLatestWeightByPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId);
        Optional<VitalsRecord> latestGlucose = vitalsRecordRepository.findLatestBloodGlucoseByPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId);
        Optional<VitalsRecord> latestBP = vitalsRecordRepository.findLatestBloodPressureByPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId);
        Optional<HealthProfile> healthProfile = healthProfileRepository.findByGlobalPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId);
        Double bmi = null;
        String bmiCategory = null;
        String bmiQuote = null;
        if (latestWeight.isPresent() && healthProfile.isPresent() && healthProfile.get().getHeightCm() != null) {
            Double weight = latestWeight.get().getWeightKg();
            Double height = healthProfile.get().getHeightCm();
            if (weight != null && height != null && height > 0) {
                double heightInMeters = height / 100.0;
                bmi = weight / (heightInMeters * heightInMeters);
                bmi = BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP).doubleValue();
                bmiCategory = getBmiCategory(bmi);
                bmiQuote = getBmiQuote(bmiCategory);
            }
        }
        return LatestVitalsResponse.builder()
                .latestWeight(latestWeight.map(VitalsRecord::getWeightKg).orElse(null))
                .latestWeightDate(latestWeight.map(VitalsRecord::getRecordedAt).orElse(null))
                .latestBloodGlucose(latestGlucose.map(VitalsRecord::getBloodGlucoseMgdl).orElse(null))
                .latestBloodGlucoseDate(latestGlucose.map(VitalsRecord::getRecordedAt).orElse(null))
                .latestBloodPressureSystolic(latestBP.map(VitalsRecord::getBloodPressureSystolic).orElse(null))
                .latestBloodPressureDiastolic(latestBP.map(VitalsRecord::getBloodPressureDiastolic).orElse(null))
                .latestBloodPressureDisplay(latestBP.map(bp -> bp.getBloodPressureSystolic() + "/" + bp.getBloodPressureDiastolic()).orElse(null))
                .latestBloodPressureDate(latestBP.map(VitalsRecord::getRecordedAt).orElse(null))
                .bmi(bmi)
                .bmiCategory(bmiCategory)
                .bmiQuote(bmiQuote)
                .disclaimer("Data may be hospital-uploaded only. For tracking purposes. Consult a healthcare professional for medical advice.")
                .build();
    }

    @Override
    public List<VitalsRecordResponse> getVitalsHistoryForHospitalContext(Long globalPatientId, Long hospitalId, VitalsHistoryRequest request) {
        globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        boolean hasPermission = vitalsPermissionRepository.hasPermission(globalPatientId, null, hospitalId);
        List<VitalsRecord> records;
        if (hasPermission) {
            if (request.getStartDate() != null || request.getEndDate() != null) {
                records = vitalsRecordRepository.findByPatientIdAndDateRange(globalPatientId, request.getStartDate(), request.getEndDate());
            } else {
                records = vitalsRecordRepository.findByGlobalPatientIdOrderByRecordedAtDesc(globalPatientId);
            }
        } else {
            if (request.getStartDate() != null || request.getEndDate() != null) {
                records = vitalsRecordRepository.findByPatientIdAndDateRangeAndUploadedByHospitalId(
                        globalPatientId, request.getStartDate(), request.getEndDate(), hospitalId);
            } else {
                records = vitalsRecordRepository.findByGlobalPatientIdAndUploadedByHospitalIdOrderByRecordedAtDesc(globalPatientId, hospitalId);
            }
        }
        if (request.getVitalType() != null && request.getVitalType() != VitalType.ALL) {
            records = filterByVitalType(records, request.getVitalType());
        }
        return records.stream().map(this::mapToVitalsRecordResponse).collect(Collectors.toList());
    }

    @Override
    public VitalsRecordResponse createVitalsRecordForHospital(Long globalPatientId, Long hospitalId, Long doctorId, VitalsRecordRequest request) {
        if (!request.hasAtLeastOneVital()) {
            throw new RuntimeException("At least one vital value is required");
        }
        GlobalPatient patient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        VitalsRecord record = new VitalsRecord();
        record.setGlobalPatient(patient);
        record.setWeightKg(request.getWeightKg());
        record.setBloodGlucoseMgdl(request.getBloodGlucoseMgdl());
        record.setBloodPressureSystolic(request.getBloodPressureSystolic());
        record.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        record.setRecordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : LocalDateTime.now());
        record.setNotes(request.getNotes());
        record.setUploadedByHospitalId(hospitalId);
        record.setUploadedByDoctorId(doctorId);
        record = vitalsRecordRepository.save(record);
        return mapToVitalsRecordResponse(record);
    }

    @Override
    public BmiReportResponse getBmiReportForHospitalContext(Long globalPatientId, Long hospitalId) {
        globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + globalPatientId));
        boolean hasHealthOrVitals = healthPermissionRepository.hasPermission(globalPatientId, null, hospitalId)
                || vitalsPermissionRepository.hasPermission(globalPatientId, null, hospitalId);
        if (hasHealthOrVitals) {
            return getBmiReport(globalPatientId);
        }
        Optional<HealthProfile> healthProfile = healthProfileRepository.findByGlobalPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId);
        Optional<VitalsRecord> latestWeight = vitalsRecordRepository.findLatestWeightByPatientIdAndUploadedByHospitalId(globalPatientId, hospitalId);
        if (healthProfile.isEmpty() || healthProfile.get().getHeightCm() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Health profile (height) not found or access denied. Request HEALTH access or add health profile.");
        }
        if (latestWeight.isEmpty() || latestWeight.get().getWeightKg() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No weight record found or access denied. Request VITALS access or add vitals for this patient.");
        }
        Double weight = latestWeight.get().getWeightKg();
        Double height = healthProfile.get().getHeightCm();
        double heightInMeters = height / 100.0;
        double bmi = weight / (heightInMeters * heightInMeters);
        bmi = BigDecimal.valueOf(bmi).setScale(2, RoundingMode.HALF_UP).doubleValue();
        String category = getBmiCategory(bmi);
        String description = getBmiDescription(category);
        String quote = getBmiQuote(category);
        String recommendation = getBmiRecommendation(category);
        return BmiReportResponse.builder()
                .bmi(bmi)
                .bmiCategory(category)
                .bmiDescription(description)
                .inspirationalQuote(quote)
                .heightCm(height)
                .weightKg(weight)
                .weightRecordedAt(latestWeight.get().getRecordedAt())
                .healthRecommendation(recommendation)
                .disclaimer("BMI from hospital-uploaded data only. Request HEALTH/VITALS access for full report. Consult a healthcare professional for medical advice.")
                .build();
    }
    
    // Helper methods
    
    private HealthProfileResponse mapToHealthProfileResponse(HealthProfile profile) {
        return HealthProfileResponse.builder()
            .id(profile.getId())
            .globalPatientId(profile.getGlobalPatient().getId())
            .heightCm(profile.getHeightCm())
            .bloodGroup(profile.getBloodGroup())
            .gender(profile.getGender())
            .dateOfBirth(profile.getDateOfBirth())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .build();
    }
    
    private VitalsRecordResponse mapToVitalsRecordResponse(VitalsRecord record) {
        String bpDisplay = null;
        if (record.getBloodPressureSystolic() != null && record.getBloodPressureDiastolic() != null) {
            bpDisplay = record.getBloodPressureSystolic() + "/" + record.getBloodPressureDiastolic();
        }
        
        return VitalsRecordResponse.builder()
            .id(record.getId())
            .globalPatientId(record.getGlobalPatient().getId())
            .weightKg(record.getWeightKg())
            .bloodGlucoseMgdl(record.getBloodGlucoseMgdl())
            .bloodPressureSystolic(record.getBloodPressureSystolic())
            .bloodPressureDiastolic(record.getBloodPressureDiastolic())
            .bloodPressureDisplay(bpDisplay)
            .recordedAt(record.getRecordedAt())
            .notes(record.getNotes())
            .createdAt(record.getCreatedAt())
            .updatedAt(record.getUpdatedAt())
            .build();
    }
    
    private String getBmiCategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25) {
            return "Normal";
        } else if (bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
    
    private String getBmiDescription(String category) {
        switch (category) {
            case "Underweight":
                return "Your BMI indicates you may be underweight. Consider consulting with a healthcare provider about healthy weight gain strategies.";
            case "Normal":
                return "Congratulations! Your BMI is within the healthy range. Keep maintaining your healthy lifestyle.";
            case "Overweight":
                return "Your BMI suggests you may be overweight. Consider working with a healthcare provider to develop a healthy weight management plan.";
            case "Obese":
                return "Your BMI indicates obesity. It's important to work with healthcare professionals to develop a comprehensive health and weight management plan.";
            default:
                return "";
        }
    }
    
    private String getBmiQuote(String category) {
        String[] quotes;
        switch (category) {
            case "Underweight":
                quotes = UNDERWEIGHT_QUOTES;
                break;
            case "Normal":
                quotes = NORMAL_QUOTES;
                break;
            case "Overweight":
                quotes = OVERWEIGHT_QUOTES;
                break;
            case "Obese":
                quotes = OBESE_QUOTES;
                break;
            default:
                return "Your health journey is unique and valuable.";
        }
        return quotes[(int) (Math.random() * quotes.length)];
    }
    
    private String getBmiRecommendation(String category) {
        switch (category) {
            case "Underweight":
                return "Focus on nutrient-dense foods and consider strength training. Consult a nutritionist for personalized guidance.";
            case "Normal":
                return "Maintain your current healthy habits: balanced diet, regular exercise, and adequate sleep.";
            case "Overweight":
                return "Consider a balanced diet with portion control and regular physical activity. Consult a healthcare provider for a personalized plan.";
            case "Obese":
                return "Work with healthcare professionals to create a comprehensive plan including diet, exercise, and lifestyle modifications.";
            default:
                return "Consult with a healthcare professional for personalized health recommendations.";
        }
    }
    
    private List<VitalsRecord> filterByVitalType(List<VitalsRecord> records, VitalType vitalType) {
        List<VitalsRecord> filtered = new ArrayList<>();
        for (VitalsRecord record : records) {
            switch (vitalType) {
                case WEIGHT:
                    if (record.getWeightKg() != null) {
                        filtered.add(record);
                    }
                    break;
                case BLOOD_GLUCOSE:
                    if (record.getBloodGlucoseMgdl() != null) {
                        filtered.add(record);
                    }
                    break;
                case BLOOD_PRESSURE:
                    if (record.getBloodPressureSystolic() != null && record.getBloodPressureDiastolic() != null) {
                        filtered.add(record);
                    }
                    break;
                default:
                    filtered.add(record);
            }
        }
        return filtered;
    }
}

