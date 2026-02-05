package com.medidropbox.service.impl;

import com.medidropbox.dto.request.HospitalSettingsRequest;
import com.medidropbox.dto.response.HospitalSettingsResponse;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.HospitalSettings;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.HospitalSettingsRepository;
import com.medidropbox.service.HospitalSettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class HospitalSettingsServiceImpl implements HospitalSettingsService {
    
    private final HospitalSettingsRepository settingsRepository;
    private final HospitalRepository hospitalRepository;
    
    public HospitalSettingsServiceImpl(
            HospitalSettingsRepository settingsRepository,
            HospitalRepository hospitalRepository) {
        this.settingsRepository = settingsRepository;
        this.hospitalRepository = hospitalRepository;
    }
    
    @Override
    public HospitalSettingsResponse getSettingsByHospitalId(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        HospitalSettings settings = settingsRepository.findByHospitalId(hospitalId)
                .orElseGet(() -> createDefaultSettings(hospital));
        
        return mapToResponse(settings);
    }
    
    @Override
    public HospitalSettingsResponse createOrUpdateSettings(Long hospitalId, HospitalSettingsRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        HospitalSettings settings = settingsRepository.findByHospitalId(hospitalId)
                .orElseGet(() -> {
                    HospitalSettings newSettings = new HospitalSettings();
                    newSettings.setHospital(hospital);
                    return newSettings;
                });
        
        // Update settings
        settings.setBookingMode(request.getBookingMode());
        settings.setFutureBookingDays(request.getFutureBookingDays());
        settings.setBookingStartTime(request.getBookingStartTime());
        settings.setBookingEndTime(request.getBookingEndTime());
        settings.setDailyPatientLimit(request.getDailyPatientLimit());
        settings.setOnlineBookingLimit(request.getOnlineBookingLimit());
        settings.setIsOnlineBookingEnabled(request.getIsOnlineBookingEnabled());
        settings.setBookingAllowed(request.getBookingAllowed());
        settings.setPendingPatientAction(request.getPendingPatientAction());
        settings.setGracePeriodDays(request.getGracePeriodDays());
        settings.setMaxRecurringVisits(request.getMaxRecurringVisits());
        settings.setRevisitQueueInterval(request.getRevisitQueueInterval());
        settings.setPaymentQrImageUrl(request.getPaymentQrImageUrl());
        
        settings = settingsRepository.save(settings);
        
        return mapToResponse(settings);
    }
    
    @Override
    public HospitalSettings getSettingsEntity(Long hospitalId) {
        return settingsRepository.findByHospitalId(hospitalId)
                .orElseGet(() -> {
                    Hospital hospital = hospitalRepository.findById(hospitalId)
                            .orElseThrow(() -> new RuntimeException("Hospital not found"));
                    return createDefaultSettings(hospital);
                });
    }
    
    private HospitalSettings createDefaultSettings(Hospital hospital) {
        HospitalSettings settings = new HospitalSettings();
        settings.setHospital(hospital);
        // Default values are set in entity, but we can override here if needed
        return settingsRepository.save(settings);
    }
    
    private HospitalSettingsResponse mapToResponse(HospitalSettings settings) {
        return HospitalSettingsResponse.builder()
                .id(settings.getId())
                .hospitalId(settings.getHospital().getId())
                .hospitalName(settings.getHospital().getName())
                .bookingMode(settings.getBookingMode())
                .futureBookingDays(settings.getFutureBookingDays())
                .bookingStartTime(settings.getBookingStartTime())
                .bookingEndTime(settings.getBookingEndTime())
                .dailyPatientLimit(settings.getDailyPatientLimit())
                .onlineBookingLimit(settings.getOnlineBookingLimit())
                .isOnlineBookingEnabled(settings.getIsOnlineBookingEnabled())
                .bookingAllowed(settings.getBookingAllowed())
                .pendingPatientAction(settings.getPendingPatientAction())
                .gracePeriodDays(settings.getGracePeriodDays())
                .maxRecurringVisits(settings.getMaxRecurringVisits())
                .revisitQueueInterval(settings.getRevisitQueueInterval())
                .paymentQrImageUrl(settings.getPaymentQrImageUrl())
                .build();
    }
}

