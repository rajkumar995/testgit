package com.medidropbox.service;

import com.medidropbox.dto.request.HospitalSettingsRequest;
import com.medidropbox.dto.response.HospitalSettingsResponse;

public interface HospitalSettingsService {
    /**
     * Get settings for a hospital (creates default if not exists)
     */
    HospitalSettingsResponse getSettingsByHospitalId(Long hospitalId);
    
    /**
     * Create or update settings for a hospital
     */
    HospitalSettingsResponse createOrUpdateSettings(Long hospitalId, HospitalSettingsRequest request);
    
    /**
     * Get settings entity (internal use for validation)
     */
    com.medidropbox.entity.HospitalSettings getSettingsEntity(Long hospitalId);
}

