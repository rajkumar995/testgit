package com.medidropbox.service.impl;

import com.medidropbox.dto.request.LabRequestCreateRequest;
import com.medidropbox.dto.request.LabRequestUpdateRequest;
import com.medidropbox.dto.response.LabRequestResponse;
import com.medidropbox.entity.*;
import com.medidropbox.enums.LabRequestStatus;
import com.medidropbox.exception.BadRequestException;
import com.medidropbox.exception.ResourceNotFoundException;
import com.medidropbox.repository.*;
import com.medidropbox.service.LabRequestService;
import com.medidropbox.util.BarcodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LabRequestServiceImpl implements LabRequestService {
    
    private final LabRequestRepository labRequestRepository;
    private final HospitalPatientRepository hospitalPatientRepository;
    private final LabTestRepository labTestRepository;
    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final LabReportRepository labReportRepository;
    
    @Override
    @Transactional
    public LabRequestResponse createRequest(Long requestingHospitalId, Long requestingDoctorId, LabRequestCreateRequest request) {
        // Validate patient
        HospitalPatient patient = hospitalPatientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        
        // Validate lab test
        LabTest labTest = labTestRepository.findById(request.getLabTestId())
            .orElseThrow(() -> new ResourceNotFoundException("Lab test not found"));
        
        // Validate requesting hospital or doctor
        if (requestingHospitalId == null && requestingDoctorId == null) {
            throw new BadRequestException("Either hospital ID or doctor ID must be provided");
        }
        
        Hospital requestingHospital = null;
        if (requestingHospitalId != null) {
            requestingHospital = hospitalRepository.findById(requestingHospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        }
        
        Doctor requestingDoctor = null;
        if (requestingDoctorId != null) {
            requestingDoctor = doctorRepository.findById(requestingDoctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        }
        
        // Validate assigned lab (if provided)
        Hospital assignedLab = null;
        if (request.getAssignedLabId() != null) {
            assignedLab = hospitalRepository.findById(request.getAssignedLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned lab not found"));
        }
        
        // Generate unique barcode
        String barcode = BarcodeGenerator.generateBarcode();
        
        // Check for duplicate barcode (very rare, but handle it)
        while (labRequestRepository.findByBarcodeAndIsActiveTrue(barcode).isPresent()) {
            barcode = BarcodeGenerator.generateBarcode();
        }
        
        // Create lab request
        LabRequest labRequest = new LabRequest();
        labRequest.setRequestingHospital(requestingHospital);
        labRequest.setRequestingDoctor(requestingDoctor);
        labRequest.setPatient(patient);
        labRequest.setLabTest(labTest);
        labRequest.setAssignedLab(assignedLab);
        labRequest.setStatus(LabRequestStatus.REQUESTED);
        labRequest.setBarcode(barcode);
        labRequest.setRequestNotes(request.getRequestNotes());
        labRequest.setPriority(request.getPriority() != null ? request.getPriority() : "NORMAL");
        labRequest.setIsActive(true);
        
        LabRequest saved = labRequestRepository.save(labRequest);
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LabRequestResponse> getLabRequests(Long labId, Pageable pageable) {
        return labRequestRepository.findByAssignedLabIdAndIsActiveTrueOrderByCreatedAtDesc(labId, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LabRequestResponse> getLabRequestsByStatus(Long labId, String status, Pageable pageable) {
        LabRequestStatus requestStatus = LabRequestStatus.valueOf(status.toUpperCase());
        return labRequestRepository.findByAssignedLabIdAndStatusAndIsActiveTrueOrderByCreatedAtDesc(labId, requestStatus, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LabRequestResponse> getHospitalRequests(Long hospitalId, Pageable pageable) {
        return labRequestRepository.findByRequestingHospitalIdAndIsActiveTrueOrderByCreatedAtDesc(hospitalId, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LabRequestResponse> getDoctorRequests(Long doctorId, Pageable pageable) {
        return labRequestRepository.findByRequestingDoctorIdAndIsActiveTrueOrderByCreatedAtDesc(doctorId, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<LabRequestResponse> getPatientRequests(Long patientId, Pageable pageable) {
        return labRequestRepository.findByPatientIdAndIsActiveTrueOrderByCreatedAtDesc(patientId, pageable)
            .map(this::mapToResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LabRequestResponse getRequestById(Long requestId) {
        LabRequest request = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        if (!request.getIsActive()) {
            throw new ResourceNotFoundException("Lab request not found");
        }
        
        return mapToResponse(request);
    }
    
    @Override
    @Transactional(readOnly = true)
    public LabRequestResponse getRequestByBarcode(String barcode) {
        LabRequest request = labRequestRepository.findByBarcodeAndIsActiveTrue(barcode)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found with barcode: " + barcode));
        
        return mapToResponse(request);
    }
    
    @Override
    @Transactional
    public LabRequestResponse updateRequest(Long requestId, LabRequestUpdateRequest request) {
        LabRequest labRequest = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        if (!labRequest.getIsActive()) {
            throw new ResourceNotFoundException("Lab request not found");
        }
        
        // Update status
        if (request.getStatus() != null) {
            labRequest.setStatus(request.getStatus());
            
            // Set timestamps based on status
            if (request.getStatus() == LabRequestStatus.SAMPLE_RECEIVED) {
                labRequest.setSampleReceivedAt(LocalDateTime.now());
                if (request.getSampleReceivedBy() != null) {
                    labRequest.setSampleReceivedBy(request.getSampleReceivedBy());
                }
            } else if (request.getStatus() == LabRequestStatus.IN_PROGRESS) {
                labRequest.setTestStartedAt(LocalDateTime.now());
            } else if (request.getStatus() == LabRequestStatus.COMPLETED) {
                labRequest.setTestCompletedAt(LocalDateTime.now());
            } else if (request.getStatus() == LabRequestStatus.SAMPLE_REJECTED) {
                labRequest.setRejectionReason(request.getRejectionReason());
            }
        }
        
        // Update assigned lab
        if (request.getAssignedLabId() != null) {
            Hospital assignedLab = hospitalRepository.findById(request.getAssignedLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned lab not found"));
            labRequest.setAssignedLab(assignedLab);
        }
        
        // Update sample received by
        if (request.getSampleReceivedBy() != null) {
            labRequest.setSampleReceivedBy(request.getSampleReceivedBy());
        }
        
        // Update rejection reason
        if (request.getRejectionReason() != null) {
            labRequest.setRejectionReason(request.getRejectionReason());
        }
        
        LabRequest saved = labRequestRepository.save(labRequest);
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public LabRequestResponse markSampleReceived(Long requestId, String receivedBy) {
        LabRequest labRequest = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        if (!labRequest.getIsActive()) {
            throw new ResourceNotFoundException("Lab request not found");
        }
        
        if (labRequest.getStatus() != LabRequestStatus.REQUESTED) {
            throw new BadRequestException("Sample can only be received for REQUESTED status");
        }
        
        labRequest.setStatus(LabRequestStatus.SAMPLE_RECEIVED);
        labRequest.setSampleReceivedAt(LocalDateTime.now());
        labRequest.setSampleReceivedBy(receivedBy);
        
        LabRequest saved = labRequestRepository.save(labRequest);
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public LabRequestResponse startTest(Long requestId) {
        LabRequest labRequest = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        if (!labRequest.getIsActive()) {
            throw new ResourceNotFoundException("Lab request not found");
        }
        
        if (labRequest.getStatus() != LabRequestStatus.SAMPLE_RECEIVED) {
            throw new BadRequestException("Test can only be started when sample is received");
        }
        
        labRequest.setStatus(LabRequestStatus.IN_PROGRESS);
        labRequest.setTestStartedAt(LocalDateTime.now());
        
        LabRequest saved = labRequestRepository.save(labRequest);
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public LabRequestResponse completeTest(Long requestId, Long labReportId) {
        LabRequest labRequest = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        if (!labRequest.getIsActive()) {
            throw new ResourceNotFoundException("Lab request not found");
        }
        
        if (labRequest.getStatus() != LabRequestStatus.IN_PROGRESS) {
            throw new BadRequestException("Test can only be completed when status is IN_PROGRESS");
        }
        
        // Validate lab report
        LabReport labReport = labReportRepository.findById(labReportId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab report not found"));
        
        labRequest.setStatus(LabRequestStatus.COMPLETED);
        labRequest.setTestCompletedAt(LocalDateTime.now());
        labRequest.setLabReport(labReport);
        
        LabRequest saved = labRequestRepository.save(labRequest);
        
        return mapToResponse(saved);
    }
    
    @Override
    @Transactional
    public void cancelRequest(Long requestId, String reason) {
        LabRequest labRequest = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        if (!labRequest.getIsActive()) {
            throw new ResourceNotFoundException("Lab request not found");
        }
        
        if (labRequest.getStatus() == LabRequestStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed request");
        }
        
        labRequest.setStatus(LabRequestStatus.CANCELLED);
        if (reason != null) {
            labRequest.setRequestNotes((labRequest.getRequestNotes() != null ? labRequest.getRequestNotes() + "\n" : "") + "Cancelled: " + reason);
        }
        
        labRequestRepository.save(labRequest);
    }
    
    @Override
    @Transactional
    public void deleteRequest(Long requestId) {
        LabRequest labRequest = labRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Lab request not found"));
        
        labRequest.setIsActive(false);
        labRequestRepository.save(labRequest);
    }
    
    private LabRequestResponse mapToResponse(LabRequest request) {
        LabRequestResponse.LabRequestResponseBuilder builder = LabRequestResponse.builder()
            .id(request.getId())
            .patientId(request.getPatient().getId())
            .patientName(request.getPatient().getFullName())
            .patientPhone(request.getPatient().getPhone())
            .labTestId(request.getLabTest().getId())
            .labTestName(request.getLabTest().getName())
            .labTestCode(request.getLabTest().getCode())
            .status(request.getStatus())
            .barcode(request.getBarcode())
            .requestNotes(request.getRequestNotes())
            .sampleReceivedAt(request.getSampleReceivedAt())
            .sampleReceivedBy(request.getSampleReceivedBy())
            .testStartedAt(request.getTestStartedAt())
            .testCompletedAt(request.getTestCompletedAt())
            .rejectionReason(request.getRejectionReason())
            .priority(request.getPriority())
            .createdAt(request.getCreatedAt())
            .updatedAt(request.getUpdatedAt());
        
        if (request.getRequestingHospital() != null) {
            builder.requestingHospitalId(request.getRequestingHospital().getId())
                   .requestingHospitalName(request.getRequestingHospital().getName());
        }
        
        if (request.getRequestingDoctor() != null) {
            builder.requestingDoctorId(request.getRequestingDoctor().getId())
                   .requestingDoctorName(request.getRequestingDoctor().getName());
        }
        
        if (request.getAssignedLab() != null) {
            builder.assignedLabId(request.getAssignedLab().getId())
                   .assignedLabName(request.getAssignedLab().getName());
        }
        
        if (request.getLabReport() != null) {
            builder.labReportId(request.getLabReport().getId())
                   .labReportUrl(request.getLabReport().getFileUrl());
        }
        
        return builder.build();
    }
}
