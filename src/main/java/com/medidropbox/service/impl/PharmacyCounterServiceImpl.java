package com.medidropbox.service.impl;

import com.medidropbox.dto.request.CounterCloseRequest;
import com.medidropbox.dto.request.CounterOpenRequest;
import com.medidropbox.dto.response.PharmacyCounterResponse;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.PharmacyCounter;
import com.medidropbox.exception.BadRequestException;
import com.medidropbox.exception.ResourceNotFoundException;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.PharmacyCounterRepository;
import com.medidropbox.service.PharmacyCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PharmacyCounterServiceImpl implements PharmacyCounterService {

    private final PharmacyCounterRepository counterRepository;
    private final HospitalRepository hospitalRepository;

    @Override
    public PharmacyCounterResponse openCounter(Long hospitalId, CounterOpenRequest request, String openedBy) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital", hospitalId));

        // Check if there's already an open counter
        counterRepository.findByHospitalIdAndIsOpenTrue(hospitalId)
                .ifPresent(counter -> {
                    throw new BadRequestException("Counter is already open. Please close it first.");
                });

        PharmacyCounter counter = new PharmacyCounter();
        counter.setHospital(hospital);
        counter.setCounterNumber(request.getCounterNumber() != null ? request.getCounterNumber() : "Counter-1");
        counter.setOpeningAmount(request.getOpeningAmount() != null ? request.getOpeningAmount() : BigDecimal.ZERO);
        counter.setOpenedAt(LocalDateTime.now());
        counter.setIsOpen(true);
        counter.setOpenedBy(openedBy != null ? openedBy : "system");
        counter.setNotes(request.getNotes());
        counter.setTotalSales(BigDecimal.ZERO);

        counter = counterRepository.save(counter);
        return mapToResponse(counter);
    }

    @Override
    public PharmacyCounterResponse closeCounter(Long hospitalId, Long counterId, CounterCloseRequest request, String closedBy) {
        PharmacyCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter", counterId));

        if (!counter.getHospital().getId().equals(hospitalId)) {
            throw new BadRequestException("Counter does not belong to this hospital");
        }

        if (!counter.getIsOpen()) {
            throw new BadRequestException("Counter is already closed");
        }

        counter.setClosedAt(LocalDateTime.now());
        counter.setClosingAmount(request.getClosingAmount());
        counter.setIsOpen(false);
        counter.setClosedBy(closedBy);
        if (request.getNotes() != null) {
            counter.setNotes(request.getNotes());
        }

        counter = counterRepository.save(counter);
        return mapToResponse(counter);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyCounterResponse getOpenCounter(Long hospitalId) {
        PharmacyCounter counter = counterRepository.findByHospitalIdAndIsOpenTrue(hospitalId)
                .orElse(null);
        
        if (counter == null) {
            return null;
        }
        
        return mapToResponse(counter);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyCounterResponse getCounterById(Long hospitalId, Long counterId) {
        PharmacyCounter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new ResourceNotFoundException("Counter", counterId));

        if (!counter.getHospital().getId().equals(hospitalId)) {
            throw new BadRequestException("Counter does not belong to this hospital");
        }

        return mapToResponse(counter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PharmacyCounterResponse> getAllCounters(Long hospitalId) {
        List<PharmacyCounter> counters = counterRepository.findByHospitalIdOrderByOpenedAtDesc(hospitalId);
        return counters.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PharmacyCounterResponse mapToResponse(PharmacyCounter counter) {
        // Ensure hospital is loaded to avoid LazyInitializationException
        Long hospitalId = counter.getHospital() != null ? counter.getHospital().getId() : null;
        
        return PharmacyCounterResponse.builder()
                .id(counter.getId())
                .hospitalId(hospitalId)
                .counterNumber(counter.getCounterNumber())
                .openedAt(counter.getOpenedAt())
                .closedAt(counter.getClosedAt())
                .openingAmount(counter.getOpeningAmount())
                .closingAmount(counter.getClosingAmount())
                .totalSales(counter.getTotalSales())
                .isOpen(counter.getIsOpen())
                .openedBy(counter.getOpenedBy())
                .closedBy(counter.getClosedBy())
                .notes(counter.getNotes())
                .createdAt(counter.getCreatedAt())
                .updatedAt(counter.getUpdatedAt())
                .build();
    }
}
