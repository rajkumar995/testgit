package com.medidropbox.controller;

import com.medidropbox.dto.request.HospitalSettingsRequest;
import com.medidropbox.dto.response.HospitalSettingsResponse;
import com.medidropbox.security.MediDropBoxUserDetails;
import com.medidropbox.service.HospitalSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hospitals/{hospitalId}/settings")
@Tag(name = "Hospital Settings", description = "Hospital booking settings management APIs")
public class HospitalSettingsController {
    
    private final HospitalSettingsService settingsService;
    
    public HospitalSettingsController(HospitalSettingsService settingsService) {
        this.settingsService = settingsService;
    }
    
    @GetMapping
    @Operation(summary = "Get hospital settings", description = "Get booking settings for a hospital (creates default if not exists)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'HOSPITAL_STAFF', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalSettingsResponse> getSettings(
            @PathVariable Long hospitalId,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only access their own hospital
        if (userDetails.getRole().name().equals("HOSPITAL_ADMIN")) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only access settings for your own hospital");
            }
        }
        return ResponseEntity.ok(settingsService.getSettingsByHospitalId(hospitalId));
    }
    
    @PutMapping
    @Operation(summary = "Update hospital settings", description = "Create or update booking settings for a hospital")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('HOSPITAL_ADMIN', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<HospitalSettingsResponse> updateSettings(
            @PathVariable Long hospitalId,
            @Valid @RequestBody HospitalSettingsRequest request,
            @AuthenticationPrincipal MediDropBoxUserDetails userDetails) {
        // Verify HOSPITAL_ADMIN can only update their own hospital
        if (userDetails.getRole().name().equals("HOSPITAL_ADMIN")) {
            Long userHospitalId = userDetails.getHospitalId();
            if (userHospitalId == null || !userHospitalId.equals(hospitalId)) {
                throw new RuntimeException("You can only update settings for your own hospital");
            }
        }
        return ResponseEntity.ok(settingsService.createOrUpdateSettings(hospitalId, request));
    }
}

