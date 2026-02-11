package com.medidropbox.service;

import com.medidropbox.dto.request.CounterCloseRequest;
import com.medidropbox.dto.request.CounterOpenRequest;
import com.medidropbox.dto.response.PharmacyCounterResponse;

import java.util.List;

public interface PharmacyCounterService {
    
    /**
     * Open a counter session
     */
    PharmacyCounterResponse openCounter(Long hospitalId, CounterOpenRequest request, String openedBy);
    
    /**
     * Close a counter session
     */
    PharmacyCounterResponse closeCounter(Long hospitalId, Long counterId, CounterCloseRequest request, String closedBy);
    
    /**
     * Get current open counter for hospital
     */
    PharmacyCounterResponse getOpenCounter(Long hospitalId);
    
    /**
     * Get counter by ID
     */
    PharmacyCounterResponse getCounterById(Long hospitalId, Long counterId);
    
    /**
     * Get all counters for hospital
     */
    List<PharmacyCounterResponse> getAllCounters(Long hospitalId);
}
